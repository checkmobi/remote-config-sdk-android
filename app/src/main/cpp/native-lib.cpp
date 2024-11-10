#include <jni.h>
#include <string>
#include "api_key.h"

extern "C" JNIEXPORT jstring JNICALL Java_com_checkmobi_checkmobisample_ui_StartActivity_stringFromJNI(JNIEnv *env, jobject /* this */)
{
    std::string JNI_API_KEY = ANDROID_HIDE_SECRETS_API_KEY_H;
    return env->NewStringUTF(JNI_API_KEY.c_str());
}
