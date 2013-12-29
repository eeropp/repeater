# Copyright (C) 2010 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_ARM_MODE := arm
LOCAL_CFLAGS := -DHAVE_CONFIG_H -DFPM_ARM -ffast-math -O3

#LOCAL_C_INCLUDES := \
#STLPORT_FORCE_REBUILD := true
#$(prebuilt_stdcxx_PATH)/include \
#$(prebuilt_stdcxx_PATH)/libs/$(TARGET_CPU_ABI)/include/

# for native asset manager
LOCAL_LDLIBS    += -landroid

LOCAL_MODULE    := sentence_compart_jni

LOCAL_SRC_FILES := decodeclient.cpp \
bit.c \
decoder.c \
fixed.c \
frame.c \
huffman.c \
layer12.c \
layer3.c \
minimad.c \
stream.c \
synth.c \
timer.c \
version.c

# for native audio
#LOCAL_LDLIBS    += -lOpenSLES
# for logging
LOCAL_LDLIBS    += -llog
# for native asset manager
#LOCAL_LDLIBS    += -landroid

include $(BUILD_SHARED_LIBRARY)
