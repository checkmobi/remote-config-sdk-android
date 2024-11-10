# remote-config-sdk-android

*[CheckMobi][1] Remote Config SDK For Android*

## Overview

CheckMobi Remote Config SDK for Android enables users to integrate CheckMobi’s verification methods in a flexible, time-saving way, eliminating the need to write custom validation logic.

## Features

- Quick integration with minimal code.
- Supports theme customization.
- Allows real-time updates to the verification flow directly from the CheckMobi website, without requiring a new app release.
- Offers a suite of verification options (SMS, Voice, Missed Call) that can be tested immediately with minimal code.
- Enables customized validation flows based on country, operator, or number, and supports A/B testing for optimization.
- Open-source, allowing for full UI customization if needed.

## Testing

A [demo app][2] is available in the repository to test the SDK without integrating it into a new project. To use it:

1. Clone the repository.
2. Open the project in [Android Studio][3].
3. Open [StartActivity.java][4] and set the variable `CHECKMOBI_SECRET_KEY` to your CheckMobi Secret Key from the web portal. Alternatively, you can store it in native code by adding it to `/src/main/cpp/api_key.h`.
4. Run the project on a device.

## Integrate the SDK into your project

To integrate the SDK (using [Android Studio][3]), follow these steps:

1. Clone the project.
2. In Android Studio, navigate to: `File -> New -> Import Module`.
3. Select the module source (`<path to cloned project>/checkmobi`) and press `Finish`.

### Setting the API Secret Key

Before using the SDK, set the CheckMobi Secret Key obtained from the web portal. This should be done before calling any SDK method:

```java
CheckmobiSdk.getInstance().setApiKey("YOUR_SERET_KEY_HERE");
```

#### Securing the Secret Key

While client-side security is never foolproof, there are several methods to secure a secret key on the client side, making it significantly harder to break:

- Use [DexGuard][11] for code obfuscation and protection.
- Store the key on your own backend instead of in the client.
- Embed the key within native code using the NDK.

A strong approach for client-side protection is to embed the secret key within native code using NDK. Here's a quick guide to implementing this:

- Copy the `cpp` folder from `/src/main` into your project.
- Place your secret key in `api_key.h` by modifying the following line:
```cpp
#define ANDROID_HIDE_SECRETS_API_KEY_H "YOUR_SECRET_KEY_HERE"
```
- Create a Java class to retrieve the key from native code. For example (as shown in `StartActivity.java`) and add the following:

```java
static {
    System.loadLibrary("native-lib");
}

public native String stringFromJNI(); // intentionally we didn't used an explicit name like getSecretKey
```
- Update the method signature in `cpp/native-lib.cpp` to reflect your package and class names. For example, change:
```cpp
Java_com_checkmobi_checkmobisample_ui_StartActivity_stringFromJNI
```
to
```cpp
Java_[your_package_name]_[your_class_name]_[your_java_function_name]
```

- Modify your `build.gradle` file to include native build settings:

```gradle
defaultConfig {
    ...
    externalNativeBuild {
        cmake {
            cppFlags ""
        }
    }
}

externalNativeBuild {
    cmake {
        path "src/main/cpp/CMakeLists.txt"
    }
}
```

### Integrating the Phone Validation Process

To check if a user has already verified their phone number:

```java
String verifiedNumber = CheckmobiSdk.getInstance().getVerifiedNumber(<context>);
```

If `verifiedNumber` is not null, the user has verified their number and can continue using the app. Otherwise, start the validation process:

```java
startActivityForResult(
    CheckmobiSdk.getInstance()
        .createVerificationIntentBuilder()
        .build(StartActivity.this), VERIFICATION_RC);
```

Override `onActivityResult` to handle the verification result:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == VERIFICATION_RC) {
        if (resultCode == RESULT_OK) {
            //The user has verified his phone number successfully
        } else {
            //The user canceled the verification process
        }
    }
}
```

### Optional Server to Server Validation

For an extra layer of security, you can check from your backend that the phone number verification actually happened. For this, you will need the server id of the verification request. You can obtain it like this after a succesful phone number verification:

```java
String verifiedNumberServerId = CheckmobiSdk.getInstance().getVerifiedNumberServerId(<context>);
```

You should send this id from the app to your backend and the backend should call the checkmobi api with it. You can find more details on how to check the status of a verification request from your backend [here][10].

### Customizations

You can change the theme used in the activities by setting you theme in the `VerificationIntentBuilder` before you start it like so:

```java
startActivityForResult(
    CheckmobiSdk.getInstance()
        .createVerificationIntentBuilder()
        .setTheme(<your theme>)
        .build(StartActivity.this), VERIFICATION_RC);
```

Since this is an Android module, if you need to customize it even more, you are free to change the code.

## Behind the scene

Behind the scene the SDK is using the [CheckMobi REST API][5].

First is doing a call to [Get Remote Config Profile][6] which returns the validation flow for the specified destination as
configured in the CheckMobi Web Portal.

Then based on the profile received the app it's using the [Request Validation API][7] and [Verify PIN API][8] to implement the desired validation processes.

The select country picker is populated using the information received from [Get Countries API][9].

[1]:https://checkmobi.com/
[2]:https://github.com/checkmobi/remote-config-sdk-android/tree/master/app/src/main/java/com/checkmobi/checkmobisample/ui
[3]:https://developer.android.com/studio
[4]:https://github.com/checkmobi/remote-config-sdk-android/blob/master/app/src/main/java/com/checkmobi/checkmobisample/ui/StartActivity.java
[5]:https://checkmobi.com/documentation/api-reference/
[6]:https://checkmobi.com/documentation/api-reference/#get-remote-config-profile
[7]:https://checkmobi.com/documentation/api-reference/#request-validation
[8]:https://checkmobi.com/documentation/api-reference/#verify-pin
[9]:https://checkmobi.com/documentation/api-reference/#get-available-countries
[10]:https://checkmobi.com/documentation/api-reference/#get-validation-status
[11]:https://www.guardsquare.com/dexguard
