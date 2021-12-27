package com.snapchat.kit.sdk.reactnative

import androidx.annotation.StringDef
import com.snapchat.kit.sdk.SnapLogin
import com.snapchat.kit.sdk.core.controller.LoginStateController
import com.snapchat.kit.sdk.core.networking.RefreshAccessTokenResult
import com.snapchat.kit.sdk.core.networking.RefreshAccessTokenResultError
import com.snapchat.kit.sdk.login.models.UserDataResponse
import com.snapchat.kit.sdk.login.networking.FetchUserDataCallback
import com.facebook.react.bridge.Arguments.createMap
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

class LoginKitNativeModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    private val authTokenManager = SnapLogin.getAuthTokenManager(reactContext)
    private val loginStateController = SnapLogin.getLoginStateController(reactContext)

    @StringDef(DISPLAY_NAME, EXTERNAL_ID, PROFILE_LINK, BITMOJI_ID, BITMOJI_SELFIE, BITMOJI_AVATAR, BITMOJI_PACKS_JSON)
    annotation class UserData

    @StringDef(IS_NETWORK_ERROR, STATUS_CODE)
    annotation class UserDataErrorFields

    @StringDef(
        LOGIN_KIT_LOGIN_STARTED,
        LOGIN_KIT_LOGIN_SUCCEEDED,
        LOGIN_KIT_LOGIN_FAILED,
        LOGIN_KIT_LOGOUT
    )
    annotation class LoginState

    companion object {
        // User Data constants
        const val DISPLAY_NAME = "displayName"
        const val EXTERNAL_ID = "externalId"
        const val PROFILE_LINK = "profileLink"
        const val BITMOJI_ID = "bitmojiId"
        const val BITMOJI_SELFIE = "bitmojiSelfie"
        const val BITMOJI_AVATAR = "bitmojiAvatar"
        const val BITMOJI_PACKS_JSON = "bitmojiPacksJson"

        // User Data Error constants
        const val IS_NETWORK_ERROR = "isNetworkError"
        const val STATUS_CODE = "statusCode"

        // Login State constants
        const val LOGIN_KIT_LOGIN_STARTED = "LOGIN_KIT_LOGIN_STARTED"
        const val LOGIN_KIT_LOGIN_SUCCEEDED = "LOGIN_KIT_LOGIN_SUCCEEDED"
        const val LOGIN_KIT_LOGIN_FAILED = "LOGIN_KIT_LOGIN_FAILED"
        const val LOGIN_KIT_LOGOUT = "LOGIN_KIT_LOGOUT"
    }

    init {
        val rootLoginStartListener = LoginStateController.OnLoginStartListener { sendEvent(LOGIN_KIT_LOGIN_STARTED) }

        val rootLoginStateChangedListener = object : LoginStateController.OnLoginStateChangedListener {
            override fun onLoginSucceeded() {
                sendEvent(LOGIN_KIT_LOGIN_SUCCEEDED)
            }

            override fun onLoginFailed() {
                sendEvent(LOGIN_KIT_LOGIN_FAILED)
            }

            override fun onLogout() {
                sendEvent(LOGIN_KIT_LOGOUT)
            }
        }

        reactContext.addLifecycleEventListener(object : LifecycleEventListener {
            override fun onHostResume() {
                // no-op
            }

            override fun onHostPause() {
                // no-op
            }

            override fun onHostDestroy() {
                removeLoginStateListeners(rootLoginStartListener, rootLoginStateChangedListener);
            }
        })

        loginStateController.addOnLoginStartListener(rootLoginStartListener)
        loginStateController.addOnLoginStateChangedListener(rootLoginStateChangedListener)
    }

    // region Public APIs

    override fun getName(): String {
        return "LoginKit"
    }

    @ReactMethod
    fun login(promise: Promise) {
        val onLoginStateChangedListener = object : LoginStateController.OnLoginStateChangedListener {
            override fun onLoginSucceeded() {
                promise.resolve(true)
            }

            override fun onLoginFailed() {
                promise.reject("error", "LOGIN_FAILED")
                removeLoginStateListeners(/* onLoginStartListener= */null, /* onLoginStateChangedListener= */this)
            }

            override fun onLogout() {
                // no-op
                removeLoginStateListeners(/* onLoginStartListener= */null, /* onLoginStateChangedListener= */this)
            }
        }

        loginStateController.addOnLoginStateChangedListener(onLoginStateChangedListener)
        authTokenManager.startTokenGrant()
    }

    @ReactMethod
    fun isUserLoggedIn(promise: Promise) {
        promise.resolve(authTokenManager.isUserLoggedIn)
    }

    @ReactMethod
    fun getAccessToken(promise: Promise) {
        promise.resolve(authTokenManager.accessToken)
    }

    @ReactMethod
    fun refreshAccessToken(promise: Promise) {
        val refreshAccessTokenResult = object : RefreshAccessTokenResult {
            override fun onRefreshAccessTokenSuccess(accessToken: String?) {
                if (accessToken != null) {
                    promise.resolve(accessToken)
                } else {
                    // TODO(Error_Types): Define specific Error Types rather than throwing generic free flow errors
                    promise.reject("error", RefreshAccessTokenResultError.NO_REFRESH_TOKEN.name)
                }
            }

            override fun onRefreshAccessTokenFailure(accessTokenResultError: RefreshAccessTokenResultError?) {
                // TODO(Error_Types): Define specific Error Types rather than throwing generic free flow errors
                promise.reject("error", accessTokenResultError?.name)
            }
        }

        authTokenManager.refreshAccessToken(refreshAccessTokenResult)
    }

    @ReactMethod
    fun clearToken() {
        authTokenManager.clearToken()
    }

    @ReactMethod
    fun hasAccessToScope(scope: String, promise: Promise) {
        promise.resolve(authTokenManager.hasAccessToScope(scope))
    }

    @ReactMethod
    fun fetchUserData(query: String, variables: ReadableMap?, promise: Promise) {
        val fetchUserDataCallback = object : FetchUserDataCallback {
            override fun onSuccess(userDataResponse: UserDataResponse?) {
                if (userDataResponse == null || userDataResponse.data == null) {
                    promise.resolve(createMap())
                    return
                }

                val meData = userDataResponse.data.me
                if (meData == null) {
                    promise.resolve(createMap())
                    return
                }

                val userDataMap = createMap()

                if (meData.displayName != null) {
                    userDataMap.putString(DISPLAY_NAME, meData.displayName)
                }
                if (meData.externalId != null) {
                    userDataMap.putString(EXTERNAL_ID, meData.externalId)
                }
                if (meData.profileLink != null) {
                    userDataMap.putString(PROFILE_LINK, meData.profileLink)
                }
                if (meData.bitmojiData?.id != null) {
                    userDataMap.putString(BITMOJI_ID, meData.bitmojiData?.id)
                }
                if (meData.bitmojiData?.selfie != null) {
                    userDataMap.putString(BITMOJI_SELFIE, meData.bitmojiData?.selfie)
                }
                if (meData.bitmojiData?.avatar != null) {
                    userDataMap.putString(BITMOJI_AVATAR, meData.bitmojiData?.avatar)
                }
                if (meData.bitmojiData?.packsJson != null) {
                    userDataMap.putString(BITMOJI_PACKS_JSON, meData.bitmojiData?.packsJson)
                }

                promise.resolve(userDataMap)
            }

            override fun onFailure(isNetworkError: Boolean, statusCode: Int) {
                val errorResponse = createMap()
                // TODO(Error_Types): Define specific Error Types rather than throwing generic free flow errors
                errorResponse.putBoolean(IS_NETWORK_ERROR, isNetworkError)
                errorResponse.putInt(STATUS_CODE, statusCode)
                promise.reject("error", errorResponse)
            }
        }

        SnapLogin.fetchUserData(
            reactApplicationContext,
            query,
            variables?.toHashMap(),
            fetchUserDataCallback
        )
    }

    @ReactMethod
    fun verify(phoneNumber: String, countryCode: String, promise: Promise) {
        // TODO(Error_Types): Define specific Error Types rather than throwing generic free flow errors
        promise.reject("error", "VERIFY_NOT_SUPPORTED")
    }

    @ReactMethod
    fun verifyAndLogin(phoneNumber: String, countryCode: String, promise: Promise) {
        // TODO(Error_Types): Define specific Error Types rather than throwing generic free flow errors
        promise.reject("error", "VERIFY_NOT_SUPPORTED")
    }

    // endregion

    // region Helper Methods

    private fun sendEvent(eventName: String) {
        reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, null)
    }

    private fun removeLoginStateListeners(
        onLoginStartListener: LoginStateController.OnLoginStartListener?,
        onLoginStateChangedListener: LoginStateController.OnLoginStateChangedListener
    ) {
        if (onLoginStartListener != null) {
            loginStateController.removeOnLoginStartListener(onLoginStartListener)
        }
        loginStateController.removeOnLoginStateChangedListener(onLoginStateChangedListener)
    }

    // endregion

}
