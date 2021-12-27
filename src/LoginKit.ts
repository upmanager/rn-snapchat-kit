import { NativeModules } from 'react-native';

/**
 * An enum representing the different Login states.
 */
export enum LoginState {
  /**
   * Called when the login request has started after the user has confirmed they want to approve the third party for
   * access to the scopes listed.
   */
  LOGIN_KIT_LOGIN_STARTED = 'LOGIN_KIT_LOGIN_STARTED',

  /**
   * Called when login through Snapchat has succeeded.
   */
  LOGIN_KIT_LOGIN_SUCCEEDED = 'LOGIN_KIT_LOGIN_SUCCEEDED',

  /**
   * Called when login through Snapchat has failed.
   */
  LOGIN_KIT_LOGIN_FAILED = 'LOGIN_KIT_LOGIN_FAILED',

  /**
   * Called whenever the User explicitly logs out via `clearToken()` or whenever the server returns `401`, requiring
   * forced logout.
   */
  LOGIN_KIT_LOGOUT = 'LOGIN_KIT_LOGOUT',
}

/**
 * An enum representing the different scopes your app can access. Scopes let your application declare which Login Kit
 * features it wants access to. If a scope is toggleable, the user can deny access to one scope while agreeing to grant
 * access to others.
 */
export enum UserDataScopes {
  /**
   * Grants access to the user's Snapchat display name.
   */
  DISPLAY_NAME = 'https://auth.snapchat.com/oauth2/api/user.display_name',

  /**
   * Grants access to the user's Bitmoji avatar; toggleable by user.
   */
  BITMOJI_AVATAR = 'https://auth.snapchat.com/oauth2/api/user.bitmoji.avatar',
}

/**
 * An interface representing the data model related to the active (connected) Snapchat User.
 */
export interface UserData {
  /**
   * The public display name of the user.
   */
  displayName?: string;

  /**
   * The unique identifier for this user on your app.
   */
  externalId?: string;

  /**
   * The public profile link for this Snapchat user.
   */
  profileLink?: string;

  /**
   * The bitmoji avatar id.
   */
  bitmojiId?: string;

  /**
   * The bitmoji url of the user.
   * @deprecated use 'bitmojiAvatar' instead.
   */
  bitmojiSelfie?: string;

  /**
   * The bitmoji url of the user.
   */
  bitmojiAvatar?: string;

  /**
   * A JSON blob representing the bitmoji sticker data.
   */
  bitmojiPacksJson?: string;
}

/**
 * An interface representing the successful return response for the `verify()` and `verifyAndLogin()` calls.
 *
 * Note: A successful Verify response doesn't confirm if the phone number has been verified. You still need to make the
 * Server API call to confirm the result of the verification.
 */
export interface VerifyResponse {
  /**
   * A phone ID generated for the phone number and used to check that the number was successfully verified by Snapchat.
   */
  phoneId: string | null;

  /**
   * A verify ID generated for the verify request and used with the `phoneId` to check if the phone number was
   * successfully verified by Snapchat.
   */
  verifyId: string | null;
}

export type LoginKitType = {
  /**
   * Begins the authentication flow using OAuth by linking into the Snapchat app. If the user does not have the Snapchat app installed,
   * it will open up a WebView to Snapchat’s web authentication page.
   *
   * You can register for `LoginState` updates using the `DeviceEventEmitter`, for example:
   * <pre>
   *   const eventCallbackLoginStarted = () => {
   *      // handle event emitted
   *    };
   *
   *  // Subscribing to event
   *  const loginStartedListener = DeviceEventEmitter.addListener(
   *     LoginState.LOGIN_KIT_LOGIN_STARTED,
   *     eventCallbackLoginStarted
   *   );
   *
   *  // Unsubscribing to event
   *  loginStartedListener.removeListener();
   * </pre>
   *
   * @returns Promise resolves when login is successful or rejects when login fails.
   */
  login(): Promise<void>;

  /**
   * Checks if the user is authenticated using Snapchat and has a valid auth token.
   *
   * @returns Promise resolves `true` if the user is authenticated using Snapchat, `false` otherwise.
   */
  isUserLoggedIn(): Promise<boolean>;

  /**
   * Returns the current local access token for the logged in user.
   *
   * Note: If this method returns null, the access token may need to be refreshed by calling `refreshAccessToken()`.
   *
   * @returns Promise resolves `null` if there is no access token or the access token.
   */
  getAccessToken(): Promise<string | null>;

  /**
   * Refreshes the user's access token.
   *
   * @returns Promise resolves with the new access token or rejects if an access token cannot be refreshed.
   */
  refreshAccessToken(): Promise<string>;

  /**
   * Method to clear the access token. This method is used for logging out the user.
   */
  clearToken(): void;

  /**
   * Determines whether the user has authorized the current session to have access to resources with the requested scope.
   *
   * @param scope the scope to check access.
   *
   * @returns Promise resolves `true` if the current session has access to resources with the scope, `false` otherwise.
   */
  hasAccessToScope(scope: UserDataScopes): Promise<boolean>;

  /**
   * Method to fetch user data based on the scopes requested.
   *
   * @param query     GraphQL query to request user data.
   * @param variables Record of variables for your GraphQL request.
   *
   * @returns Promise resolves with returned user data or rejects if there was an issue fetching the data.
   */
  fetchUserData(
    query: string,
    variables: Record<string, any> | null
  ): Promise<UserData>;

  /**
   * Start the flow for Verify with Snapchat.
   *
   * This checks an inputted phone number and verify that it matched the one on file for the user's Snapchat account.
   * Users will be redirected to Snapchat to verify their phone number; once the number has been confirmed, user will
   * return back to the app.
   *
   * Note: This API is currently only supported on iOS.
   *
   * @param phoneNumber A phone number with appropriate area codes that needs to be verified.
   *                    <p>
   *                    This should follow the <b>National Format</b> specified by the
   *                    <a href="https://www.itu.int/rec/T-REC-E.123-200102-I/en">ITU-T Recommendation E.123</a>.
   *                    <p>
   *                    For example, <b><i>(302) 123-4567</i></b> is the National Format for this International Number
   *                    <b><i>+1 (302) 123-4567</i></b>.
   *                    <p>
   *                    Note that the number doesn't specifically need to be formatted with Parentheses, Hyphens,
   *                    Spaces, or other separating symbols and so <b><i>3021234567</i></b> will also work.
   *
   * @param countryCode The two-letter country code (as per the
   *                    <a href="https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2">ISO 3166-1 alpha-2</a>
   *                    ) in upper-case that represents the country this phone number applies to.
   *                    <p>
   *                    For example, <b><i>US</i></b> is the country code for this International Number
   *                    <b><i>+1 (302) 123-4567</i></b>.
   *
   * @returns Promise resolves with returned verify response or rejects if there was an issue during verification.
   */
  verify(phoneNumber: string, countryCode: string): Promise<VerifyResponse>;

  /**
   * Start the flow for Verify and Login with Snapchat.
   *
   * This checks an inputted phone number and verify that it matched the one on file for the user's Snapchat account.
   * Users will be redirected to Snapchat to verify their phone number; once the number has been confirmed, they will
   * be redirected to the Login / OAuth modal. After authorization, user will return back to the app.
   *
   * Note: This API is currently only supported on iOS.
   *
   * @param phoneNumber A phone number with appropriate area codes that needs to be verified.
   *                    <p>
   *                    This should follow the <b>National Format</b> specified by the
   *                    <a href="https://www.itu.int/rec/T-REC-E.123-200102-I/en">ITU-T Recommendation E.123</a>.
   *                    <p>
   *                    For example, <b><i>(302) 123-4567</i></b> is the National Format for this International Number
   *                    <b><i>+1 (302) 123-4567</i></b>.
   *                    <p>
   *                    Note that the number doesn't specifically need to be formatted with Parentheses, Hyphens,
   *                    Spaces, or other separating symbols and so <b><i>3021234567</i></b> will also work.
   *
   * @param countryCode The two-letter country code (as per the
   *                    <a href="https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2">ISO 3166-1 alpha-2</a>
   *                    ) in upper-case that represents the country this phone number applies to.
   *                    <p>
   *                    For example, <b><i>US</i></b> is the country code for this International Number
   *                    <b><i>+1 (302) 123-4567</i></b>.
   *
   * @returns Promise resolves with returned verify response or rejects if there was an issue during verification.
   */
  verifyAndLogin(
    phoneNumber: string,
    countryCode: string
  ): Promise<VerifyResponse>;
};

const LoginKit: LoginKitType = NativeModules.LoginKit;
export { LoginKit };
