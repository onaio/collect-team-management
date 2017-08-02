#include <jni.h>
#include <string>

extern "C"
jstring
Java_io_ona_collect_android_team_MessagesActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
