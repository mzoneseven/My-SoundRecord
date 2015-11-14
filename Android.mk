LOCAL_PATH:= $(call my-dir)

#####add for lib libmp3lame.so##########
#include $(CLEAR_VARS)
#LOCAL_MODULE_TAGS := optional
#LOCAL_MODULE := libmp3lame
#LOCAL_MODULE_TAGS := optional
#LOCAL_SRC_FILES := libs/armeabi/libmp3lame.so
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_MODULE_SUFFIX := .so
#LOCAL_MULTILIB := 32
#include $(BUILD_PREBUILT)

#######################################

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

#LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JNI_SHARED_LIBRARIES += libaudiocore

#LOCAL_JNI_SHARED_LIBRARIES += libmp3lame

#LOCAL_REQUIRED_MODULES := libmp3lame

LOCAL_MULTILIB := 32

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v13 \
    android-support-v4 \
    android-support-v7-appcompat \
    com.android.themes
    


LOCAL_PACKAGE_NAME := androidRecorder
LOCAL_CERTIFICATE := platform
include $(BUILD_PACKAGE)
include $(call all-makefiles-under,$(LOCAL_PATH))
