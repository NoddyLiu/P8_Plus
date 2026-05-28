
#include <termios.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <errno.h>
#include <android/log.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <jni.h>

#define LOG_TAG "SerialPort"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static int fd = -1;

speed_t getBaudrate(jint baudrate) {
    switch (baudrate) {
        case 9600: return B9600;
        case 19200: return B19200;
        case 38400: return B38400;
        case 57600: return B57600;
        case 115200: return B115200;
        default: return -1;
    }
}

JNIEXPORT jobject JNICALL Java_com_example_x6_serial_SerialPort_open
  (JNIEnv *env, jobject thiz, jstring path, jint baudrate, jint flags) {

    const char *path_utf = (*env)->GetStringUTFChars(env, path, NULL);
    LOGI("Opening serial port: %s, baudrate: %d", path_utf, baudrate);

    speed_t speed = getBaudrate(baudrate);
    if (speed == -1) {
        LOGE("Invalid baudrate");
        return NULL;
    }

    fd = open(path_utf, O_RDWR | O_NOCTTY | O_NONBLOCK);
    if (fd == -1) {
        LOGE("Cannot open port %s: %s", path_utf, strerror(errno));
        return NULL;
    }

    struct termios cfg;
    if (tcgetattr(fd, &cfg)) {
        LOGE("tcgetattr() failed");
        close(fd);
        return NULL;
    }

    cfmakeraw(&cfg);
    cfsetispeed(&cfg, speed);
    cfsetospeed(&cfg, speed);

    if (tcsetattr(fd, TCSANOW, &cfg)) {
        LOGE("tcsetattr() failed");
        close(fd);
        return NULL;
    }

    jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");
    jmethodID constructorID = (*env)->GetMethodID(env, FileDescriptorClass, "<init>", "()V");
    jobject mFileDescriptor = (*env)->NewObject(env, FileDescriptorClass, constructorID);

    jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");
    (*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint)fd);

    (*env)->ReleaseStringUTFChars(env, path, path_utf);
    return mFileDescriptor;
}

JNIEXPORT void JNICALL Java_com_example_x6_serial_SerialPort_close
  (JNIEnv *env, jobject thiz) {
    if (fd != -1) {
        close(fd);
        fd = -1;
    }
}
