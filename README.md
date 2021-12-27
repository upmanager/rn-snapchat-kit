# Snap Kit x React Native

Official React Native bindings for Snap Kit.

## Installation

```sh
npm install rn-snapchat-kit
```

## Feautures Supported

Currently we only provide support for below kits:

- [Login Kit](https://kit.snapchat.com/login-kit)
- [Creative Kit](https://kit.snapchat.com/creative-kit)

\* _Support for other kits coming soon..._

## API & Usage

### Login Kit

API section will be updated soon and so for now refer the `LoginKit.ts` file in the source for details on the supported APIs.

```js
import { LoginKit } from 'rn-snapchat-kit';

// ...

LoginKit.login()
  .then(() => {
    // handle login success
  })
  .catch((error) => {
    // handle login failure
  });
```

### Creative Kit

API section will be updated soon and so for now refer the `CreativeKit.ts` file in the source for details on the supported APIs.

```js
import { CreativeKit } from 'rn-snapchat-kit';

// ...

CreativeKit.sharePhoto({
  content: {
    // add photo data
  },
  sticker: {
    // optional sticker data
  },
  attachmentUrl: '<optional URL to attach>',
  caption: '<optional text to attach>',
});
```

## License

Any access or use of the included software, associated documentation, software
code, or other materials made available by Snap Inc. (and its affiliates) is
subject to your agreement and acceptance (by clicking the accept button) of the
Snap Developer Terms of Service found at:

https://kit.snapchat.com/manage/eula/?viewOnly=true

If you do not wish to be a party to these terms or if you do not agree to all
of these terms, then do not use or otherwise access any such software,
documentation, software code, and other materials.
