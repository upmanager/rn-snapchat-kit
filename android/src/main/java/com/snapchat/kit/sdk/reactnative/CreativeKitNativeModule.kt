package com.snapchat.kit.sdk.reactnative

import androidx.annotation.StringDef
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Promise
import com.snapchat.kit.sdk.SnapCreative
import com.snapchat.kit.sdk.creative.exceptions.SnapKitBaseException
import com.snapchat.kit.sdk.creative.exceptions.SnapMediaSizeException
import com.snapchat.kit.sdk.creative.media.SnapPhotoFile
import com.snapchat.kit.sdk.creative.media.SnapVideoFile
import com.snapchat.kit.sdk.creative.models.SnapPhotoContent
import com.snapchat.kit.sdk.creative.models.SnapVideoContent
import java.io.File
import com.snapchat.kit.sdk.creative.exceptions.SnapStickerSizeException
import com.snapchat.kit.sdk.creative.media.SnapLensLaunchData
import com.snapchat.kit.sdk.creative.media.SnapSticker
import com.snapchat.kit.sdk.creative.models.SnapLensContent
import com.snapchat.kit.sdk.creative.models.SnapLiveCameraContent
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.InputStream
import java.lang.Exception


/**
 * Class that creates a React Native module for Creative Kit that allows React Native
 * to consume the native APIs.
 */
class CreativeKitNativeModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val snapCreativeKitApi = SnapCreative.getApi(reactContext)
  private val snapMediaFactory = SnapCreative.getMediaFactory(reactContext)

  @StringDef(
    CONTENT_KEY,
    RAW_KEY,
    URI_KEY
  )
  annotation class ContentData

  @StringDef(
    STICKER_KEY,
    URI_KEY,
    WIDTH_KEY,
    HEIGHT_KEY,
    POS_X_KEY,
    POS_Y_KEY,
    ROTATION_DEGREES_IN_CLOCKWISE_KEY
  )
  annotation class StickerData

  @StringDef(
    CAPTION_KEY
  )
  annotation class CaptionData

  @StringDef(
    ATTACHMENT_URL_KEY
  )
  annotation class AttachmentUrlData

  @StringDef(
    LENS_LAUNCH_DATA,
    LENS_UUID
  )
  annotation class LensData

  companion object {
    // Content Data Constants
    const val CONTENT_KEY = "content"
    const val RAW_KEY = "raw"
    const val URI_KEY = "uri"

    // Sticker Data Constants
    const val STICKER_KEY = "sticker"
    const val WIDTH_KEY = "width"
    const val HEIGHT_KEY = "height"
    const val POS_X_KEY = "posX"
    const val POS_Y_KEY = "posY"
    const val ROTATION_DEGREES_IN_CLOCKWISE_KEY = "rotationDegreesInClockwise"

    const val CAPTION_KEY = "caption"
    const val ATTACHMENT_URL_KEY = "attachmentUrl"

    // Lens Data Constants
    const val LENS_LAUNCH_DATA = "launchData"
    const val LENS_UUID = "lensUUID"

    const val TEMP_FILE_PREFIX = "creativeKitContent"
    const val TEMP_IMAGE_FILE_NAME = "snapImage"
    const val TEMP_VIDEO_FILE_NAME = "snapVideo"
    const val TEMP_STICKER_FILE_NAME = "sticker"

    const val TAG = "CreativeKitNativeModule"
  }

  override fun getName(): String {
    return "CreativeKit"
  }

  @ReactMethod
  fun sharePhoto(photoContent: ReadableMap, promise: Promise) {
    val photoFile: SnapPhotoFile

    var tempFile: File? = null
    val contentMap = photoContent.getMap(CONTENT_KEY)

    if (contentMap != null) {
      tempFile = if (contentMap.hasKey(RAW_KEY)) {
        saveImageRaw(reactApplicationContext, contentMap.getString(RAW_KEY).toString(), promise)
      } else {
        saveFileUri(reactApplicationContext, contentMap.getString(URI_KEY).toString(), TEMP_IMAGE_FILE_NAME, promise)
      }
    }

    if (tempFile == null) {
      rejectShareFile(promise, "Invalid image provided.", tempFile)
      return
    }

    try {
      photoFile = snapMediaFactory.getSnapPhotoFromFile(tempFile)
    } catch (e: SnapMediaSizeException) {
      Log.e(TAG, "Unable to get photo file - " + e.message)
      rejectShareFile(promise, e.localizedMessage.toString(), tempFile)
      return
    }

    val snapPhotoContent = SnapPhotoContent(photoFile)
    val snapSticker = createStickerFile(photoContent.getMap(STICKER_KEY))

    if (snapSticker != null) {
      snapPhotoContent.snapSticker = snapSticker
    }

    if (photoContent.hasKey(CAPTION_KEY)) {
      snapPhotoContent.captionText = photoContent.getString(CAPTION_KEY)
    }

    if (photoContent.hasKey(ATTACHMENT_URL_KEY)) {
      snapPhotoContent.attachmentUrl = photoContent.getString(ATTACHMENT_URL_KEY)
    }

    snapCreativeKitApi.send(snapPhotoContent)
    promise.resolve(true)
  }

  @ReactMethod
  fun shareVideo(videoContent: ReadableMap, promise: Promise) {
    var tempFile: File? = null
    val contentMap = videoContent.getMap(CONTENT_KEY)
    if (contentMap != null) {
      tempFile = saveFileUri(reactApplicationContext, contentMap.getString(URI_KEY).toString(), TEMP_VIDEO_FILE_NAME, promise)
    }

    if (tempFile == null) {
      promise.reject("error", "Invalid video provided.")
      return
    }

    val videoFile: SnapVideoFile
    try {
      videoFile = snapMediaFactory.getSnapVideoFromFile(tempFile)
    } catch (e: SnapKitBaseException) {
      Log.e(TAG, "Unable to get video file - " + e.message)
      promise.reject("error", e.localizedMessage)
      return
    }

    val snapVideoContent = SnapVideoContent(videoFile)
    val snapSticker = createStickerFile(videoContent.getMap(STICKER_KEY))

    if (snapSticker != null) {
      snapVideoContent.snapSticker = snapSticker
    }

    if (videoContent.hasKey(CAPTION_KEY)) {
      snapVideoContent.captionText = videoContent.getString(CAPTION_KEY)
    }

    if (videoContent.hasKey(ATTACHMENT_URL_KEY)) {
      snapVideoContent.attachmentUrl = videoContent.getString(ATTACHMENT_URL_KEY)
    }

    snapCreativeKitApi.send(snapVideoContent)
    promise.resolve(true)
  }

  @ReactMethod
  fun shareToCameraPreview(metadata: ReadableMap, promise: Promise) {
    val snapContent = SnapLiveCameraContent()

    if (metadata.hasKey(CAPTION_KEY)) {
      snapContent.captionText = metadata.getString(CAPTION_KEY)
    }

    if (metadata.hasKey(ATTACHMENT_URL_KEY)) {
      snapContent.attachmentUrl = metadata.getString(ATTACHMENT_URL_KEY)
    }

    val snapSticker = createStickerFile(metadata.getMap(STICKER_KEY))
    if (snapSticker != null) {
      snapContent.snapSticker = snapSticker
    }

    snapCreativeKitApi.send(snapContent)
    promise.resolve(true)
  }

  @ReactMethod
  fun shareLensToCameraPreview(lensContent: ReadableMap, promise: Promise) {
    val launchDataBuilder = SnapLensLaunchData.Builder()
    var launchData: SnapLensLaunchData? = null
    if (lensContent.hasKey(LENS_LAUNCH_DATA)) {
      val launchDataMap = lensContent.getMap(LENS_LAUNCH_DATA)

      if (launchDataMap != null) {
        launchDataMap!!.entryIterator.forEach { data ->
          launchDataBuilder.addStringKeyPair(
            data.key,
            data.value as String
          )
        }

        launchData = launchDataBuilder.build()
      }
    }

    val snapLensContent =
      SnapLensContent.createSnapLensContent(lensContent.getString(LENS_UUID).toString(), launchData)

    if (lensContent.hasKey(CAPTION_KEY)) {
      snapLensContent.captionText = lensContent.getString(CAPTION_KEY)
    }

    if (lensContent.hasKey(ATTACHMENT_URL_KEY)) {
      snapLensContent.attachmentUrl = lensContent.getString(ATTACHMENT_URL_KEY)
    }

    snapCreativeKitApi.send(snapLensContent)
    promise.resolve(true)
  }

  private fun createStickerFile(stickerMap: ReadableMap?): SnapSticker? {
    var stickerFile: File? = null
    if (stickerMap != null) {
      stickerFile = saveFileUri(reactApplicationContext, stickerMap.getString(URI_KEY).toString(), TEMP_STICKER_FILE_NAME, null)
    }

    if (stickerFile == null) {
      return null
    }

    val sticker: SnapSticker
    try {
      sticker = snapMediaFactory.getSnapStickerFromFile(stickerFile)
    } catch (e: SnapStickerSizeException) {
      Log.e(TAG, "Unable to get sticker file - " + e.message)
      return null
    }

    if (stickerMap?.hasKey(WIDTH_KEY) == true) {
      sticker.setWidthDp(stickerMap.getInt(WIDTH_KEY).toFloat())
    }

    if (stickerMap?.hasKey(HEIGHT_KEY) == true) {
      sticker.setHeightDp(stickerMap.getInt(HEIGHT_KEY).toFloat())
    }

    if (stickerMap?.hasKey(POS_X_KEY) == true) {
      sticker.setPosX(stickerMap.getDouble(POS_X_KEY).toFloat())
    }

    if (stickerMap?.hasKey(POS_Y_KEY) == true) {
      sticker.setPosY(stickerMap.getDouble(POS_Y_KEY).toFloat())
    }

    if (stickerMap?.hasKey(ROTATION_DEGREES_IN_CLOCKWISE_KEY) == true) {
      sticker.setRotationDegreesClockwise(
        stickerMap.getDouble(ROTATION_DEGREES_IN_CLOCKWISE_KEY).toFloat()
      )
    }

    return sticker
  }

  private fun rejectShareFile(promise: Promise, rejectMessage: String, tempFile: File?) {
    promise.reject("error", rejectMessage)
    if (tempFile != null) {
      deleteImage(tempFile)
    }
  }

  private fun saveImageRaw(context: Context, imageData: String?, promise: Promise): File? {
    if (imageData?.trim() == "") {
      promise.reject("error", "Invalid photo data.")
      return null
    }

    val imgBytesData = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT)
    val imagesFolder = File(context.filesDir, TEMP_FILE_PREFIX)
    if (imagesFolder.exists() && !imagesFolder.isDirectory) {
      imagesFolder.delete()
    }

    if (!imagesFolder.exists()) {
      imagesFolder.mkdir()
    }

    val snapFile = File(imagesFolder, TEMP_IMAGE_FILE_NAME)
    if (snapFile.exists()) {
      snapFile.delete()
    }
    FileOutputStream(snapFile).use { it ->
      BufferedOutputStream(it).use { bufferedOutputStream ->
        bufferedOutputStream.write(imgBytesData)
      }
    }

    return snapFile
  }

  private fun saveFileUri(context: Context, fileData: String?, fileName: String, promise: Promise?): File? {
    if (fileData?.trim() == "") {
      promise?.reject("error", "Invalid content data.")
      return null
    }

    val inputStream: InputStream?
    try {
      inputStream = context.contentResolver.openInputStream(Uri.parse(fileData))
    } catch (e: Exception) {
      Log.e(TAG, "Unable to save " + fileName + " file - " + e.message)
      if (promise !== null) {
        rejectShareFile(promise, "Unable to save $fileName file.", null);
      }
      return null
    }

    val imagesFolder = File(context.filesDir, TEMP_FILE_PREFIX)
    if (imagesFolder.exists() && !imagesFolder.isDirectory) {
      imagesFolder.delete()
    }

    if (!imagesFolder.exists()) {
      imagesFolder.mkdir()
    }

    val snapFile = File(imagesFolder, fileName)
    if (snapFile.exists()) {
      snapFile.delete()
    }

    inputStream.use { input ->
      snapFile.outputStream().use { output ->
        input?.copyTo(output)
      }
    }

    inputStream?.close()

    return snapFile
  }

  private fun deleteImage(file: File) {
    if (file.exists()) {
      file.delete()
    }
  }

}
