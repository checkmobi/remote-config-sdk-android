# remote-config-sdk-android

*[CheckMobi][1] Remote Config SDK For Android*

### Overview

CheckMobi Remote Config SDK for Android allows the users to integrate CheckMobi validation methods 
on Android in a very efficient and flexible manner without wasting their time to write the logic for any validation flow.

### Features

- Integration with few lines of code 
- Allow theme customization
- You can change the verification flow directly from the CheckMobi website, on the fly, without deploying a new client version.
- The CheckMobi complete suite of verification products (SMS, Voice, Missed Call) creates a variety of flows that you can test instantly with few lines of code.
- Customize different validation flows by country, operator or even number and split test to validate improvements.
- It's completely open source. In case the API doesn't allow you to customize the UI as you wish, you can anytime clone it and change the code.

### Testing

The repo contains a [demo app][2] which can be used to test the product without having to integrate into a new project.

In order to do this just:

- Clone the repo
- Open the project in [Android Studio][3]
- Open [StartActivity.java][4] and search for the variable `CHECKMOBI_SECRET_KEY` and set it's value to your CheckMobi Secret Key from web portal.
- Run the project on a device

### Integrate the SDK into your project

In order to integrate the SDK into your project (using [Android Studio][3]), follow the next steps:

- Clone the project
- Into [Android Studio][3] go to: `File -> New -> Import Module`
- Choose the source of the module that you just cloned (`<path to cloned project>/checkmobi`) and press `Finish`

#### Set the API Secret Key

In order to use the SDK you need in the first time to set the CheckMobi Secret Key from the web portal. You can do this somewhere before calling any SDK method by calling:

```java
CheckmobiSdk.getInstance().setApiKey("YOUR_SERET_KEY_HERE");
```

#### Integrate the phone validation process

The first thing you need to do is to check if the user has already verified his number. You can do this like so:

```java
String verifiedNumber = CheckmobiSdk.getInstance().getVerifiedNumber(<context>);
```

If `verifiedNumber` is not null, your user has verified his number and you should allow him to continue using the app otherwise 
you should redirect him to the validation process.

To start a validation process you should add the following lines of code:

```java
startActivityForResult(
    CheckmobiSdk.getInstance()
        .createVerificationIntentBuilder()
        .build(StartActivity.this), VERIFICATION_RC);
```

You should also override the `onActivityResult` method like so:

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

#### Optional Server to Server Validation

For an extra layer of security, you can check from your backend that the phone number verification actually happened. For this, you will need the server id of the verification request. You can obtain it like this after a succesful phone number verification:

```java
String verifiedNumberServerId = CheckmobiSdk.getInstance().getVerifiedNumberServerId(<context>);
```

You should send this id from the app to your backend and the backend should call the checkmobi api with it. You can find more details on how to check the status of a verification request from your backend [here][10].

#### Customizations

You can change the theme used in the activities by setting you theme in the `VerificationIntentBuilder` before you start it like so:

```java
startActivityForResult(
    CheckmobiSdk.getInstance()
        .createVerificationIntentBuilder()
        .setTheme(<your theme>)
        .build(StartActivity.this), VERIFICATION_RC);
```
 
Since this is an Android module, if you need to customize it even more, you are free to change the code.

#### Behind the scene

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
