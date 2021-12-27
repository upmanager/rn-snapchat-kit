import { NativeModules } from 'react-native';

/**
 * Interface that represents the optional sticker attributes.
 */
export interface StickerParams {
  /**
   * The width of the sticker.
   */
  width?: number;

  /**
   * The height of the sticker.
   */
  height?: number;

  /**
   * The x-coordinate position of the sticker.
   */
  posX?: number;

  /**
   * The rotation (degrees in clockwise direction) to apply on the sticker.
   */
  posY?: number;

  /**
   * The degrees to rotate the sticker clockwise direction.
   */
  rotationDegreesInClockwise?: number;

  /**
   * Enables animation for the sticker (iOS only).
   */
  isAnimated?: boolean;
}

/**
 * Base interface that represents the structure of params for each share type.
 */
export interface MetadataParams {
  /**
   * Optional sticker to attach to content.
   */
  sticker?: ImageData & StickerParams;

  /**
   * Optional caption to attach to content.
   */
  caption?: string;

  /**
   * Optional URL to attach to content.
   */
  attachmentUrl?: string;
}

/**
 * Type that represents the two supported image formats: raw (base64) and URI.
 */
export type ImageData = { raw: string } | { uri: string };

/**
 * Interface that represents params needed for photo content.
 */
export interface PhotoContentParams extends MetadataParams {
  /**
   * The image data to be shared.
   */
  content: ImageData;
}

/**
 * Type that represents the supported video format: URI.
 */
export interface VideoData {
  uri: string;
}

/**
 * Interface that represents params needed for video content.
 */
export interface VideoContentParams extends MetadataParams {
  /**
   * The video data to be shared.
   */
  content: VideoData;
}

/**
 * Interface that represents params needed for lens content.
 */
interface LensContentParams {
  /**
   * The UUID of the lens to be shared.
   */
  lensUUID: string;

  /**
   * Optional key-value pairs of additional launch data to attach to lens.
   */
  launchData?: Record<string, any>;

  /**
   * Optional caption to attach to content.
   */
  caption?: string;

  /**
   * Optional URL to attach to content.
   */
  attachmentUrl?: string;
}

export type CreativeKitType = {
  /**
   * Shares a still image content to Snapchat preview.
   *
   * <pre>
   *   CreativeKit.sharePhoto({
   *    content: {
   *      // add photo data
   *    },
   *    sticker: {
   *      // optional sticker data
   *    },
   *    attachmentUrl: "<optional URL to attach>",
   *    caption: "<optional text to attach>"
   *   });
   * </pre>
   *
   */
  sharePhoto(photoContent: PhotoContentParams): Promise<void>;

  /**
   * Shares a video content to Snapchat preview.
   *
   * <pre>
   *   CreativeKit.shareVideo({
   *    content: {
   *      // add video data
   *    },
   *    sticker: {
   *      // optional sticker data
   *    },
   *    attachmentUrl: "<optional URL to attach>",
   *    caption: "<optional text to attach>"
   *   });
   * </pre>
   *
   */
  shareVideo(videoContent: VideoContentParams): Promise<void>;

  /**
   * Opens SnapChat camera with optional metadata.
   *
   * <pre>
   *   CreativeKit.shareToCameraPreview({
   *    sticker: {
   *      // optional sticker data
   *    },
   *    attachmentUrl: "<optional URL to attach>",
   *    caption: "<optional text to attach>"
   *   });
   * </pre>
   *
   */
  shareToCameraPreview(metadata?: MetadataParams): Promise<void>;

  /**
   * Shares a lens attachment to Snapchat camera.
   *
   * <pre>
   *   CreativeKit.shareLensToCameraPreview({
   *    lensUUID: "<UUID of lens>",
   *    launchData: {
   *      // additional key-value attributes for the lens
   *    }
   *    attachmentUrl: "<optional URL to attach>",
   *    caption: "<optional text to attach>"
   *   });
   * </pre>
   *
   */
  shareLensToCameraPreview(lensContent: LensContentParams): Promise<void>;
};

const CreativeKit: CreativeKitType = NativeModules.CreativeKit;
export { CreativeKit };
