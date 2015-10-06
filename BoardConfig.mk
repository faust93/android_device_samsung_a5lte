#
# Copyright (C) 2015 The Android Open-Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

-include vendor/samsung/a5lte/BoardConfigVendor.mk

LOCAL_PATH := device/samsung/a5lte

BOARD_VENDOR := samsung

TARGET_SPECIFIC_HEADER_PATH := $(LOCAL_PATH)/include

TARGET_BOARD_PLATFORM := msm8916
TARGET_BOARD_PLATFORM_GPU := qcom-adreno306

# Bootloader
TARGET_BOOTLOADER_BOARD_NAME := msm8916
TARGET_NO_BOOTLOADER := true
TARGET_NO_RADIOIMAGE := true
TARGET_NO_RPC := true

# Architecture
TARGET_ARCH := arm
TARGET_ARCH_VARIANT := armv7-a-neon
TARGET_CPU_ABI := armeabi-v7a
TARGET_CPU_ABI2 := armeabi
TARGET_CPU_VARIANT := cortex-a53
TARGET_USE_QCOM_BIONIC_OPTIMIZATION := true
TARGET_GLOBAL_CFLAGS += -mfpu=neon -mfloat-abi=softfp
TARGET_GLOBAL_CPPFLAGS += -mfpu=neon -mfloat-abi=softfp
TARGET_CPU_SMP := true
ARCH_ARM_HAVE_TLS_REGISTER := true
TARGET_USE_QCOM_BIONIC_OPTIMIZATION := true

BOARD_CUSTOM_BOOTIMG_MK := $(LOCAL_PATH)/mkbootimg.mk
BOARD_KERNEL_BASE := 0x80000000
BOARD_KERNEL_CMDLINE := console=null androidboot.hardware=qcom user_debug=31 msm_rtb.filter=0x3F ehci-hcd.park=3 androidboot.bootdevice=7824900.sdhci
BOARD_KERNEL_PAGESIZE := 2048
BOARD_KERNEL_SEPARATED_DT := true
BOARD_KERNEL_TAGS_OFFSET=0x01E00000

# UsingpPrebuilt kernel temporarily
#BOARD_MKBOOTIMG_ARGS := --dt device/samsung/a5lte/dt.img
#TARGET_PREBUILT_KERNEL := device/samsung/a5lte/kernel

TARGET_KERNEL_SOURCE := kernel/samsung/f93_a5f
TARGET_KERNEL_CONFIG := msm8916_sec_defconfig
TARGET_KERNEL_VARIANT_CONFIG := f93_a5_defconfig
#msm8916_sec_a5_eur_defconfig
TARGET_KERNEL_SELINUX_CONFIG := selinux_defconfig

# RIL
#BOARD_PROVIDES_LIBRIL := true
BOARD_RIL_CLASS := ../../../device/samsung/a5lte/ril/SamsungA5FRIL.java

# Audio
BOARD_USES_ALSA_AUDIO := true
AUDIO_FEATURE_MDM_DETECT := true
AUDIO_FEATURE_ENABLED_FLAC_OFFLOAD := true
AUDIO_FEATURE_LOW_LATENCY_PRIMARY := true
TARGET_USES_QCOM_MM_AUDIO := true
#
AUDIO_FEATURE_ENABLED_INCALL_MUSIC := true
AUDIO_FEATURE_ENABLED_MULTIPLE_TUNNEL := true
AUDIO_FEATURE_ENABLED_PROXY_DEVICE := true

# ANT+
BOARD_ANT_WIRELESS_DEVICE := "vfs-prerelease"

# Bluetooth
BOARD_HAVE_BLUETOOTH := true
BOARD_HAVE_BLUETOOTH_QCOM := true
BOARD_BLUETOOTH_BDROID_BUILDCFG_INCLUDE_DIR := $(LOCAL_PATH)/bluetooth
BLUETOOTH_HCI_USE_MCT := true

# Camera
TARGET_USE_VENDOR_CAMERA_EXT := true
USE_DEVICE_SPECIFIC_CAMERA := true
COMMON_GLOBAL_CFLAGS += -DSAMSUNG_CAMERA_HARDWARE

# Classpath
#PRODUCT_BOOT_JARS := $(subst $(space),:,$(PRODUCT_BOOT_JARS))

# Enable suspend during charger mode
BOARD_CHARGER_ENABLE_SUSPEND := true

# Enables CSVT
TARGET_USES_CSVT := true

TARGET_USES_LOGD := true

# Flags
COMMON_GLOBAL_CFLAGS += -D_ION_HEAP_MASK_COMPATIBILITY_WA

# QC PROPRIETARY
ifneq ($(QCPATH),)
BOARD_USES_QCNE := true
endif

# Graphics
TARGET_HARDWARE_3D := false
OVERRIDE_RS_DRIVER := libRSDriver_adreno.so
MAX_EGL_CACHE_KEY_SIZE := 12*1024
MAX_EGL_CACHE_SIZE := 2048*1024
BOARD_EGL_CFG := $(LOCAL_PATH)/egl.cfg
TARGET_USES_ION := true
TARGET_USES_NEW_ION_API :=true
TARGET_USES_OVERLAY := true
NUM_FRAMEBUFFER_SURFACE_BUFFERS := 3
TARGET_USES_C2D_COMPOSITION := true
USE_OPENGL_RENDERER := true
TARGET_CONTINUOUS_SPLASH_ENABLED := true

# Hardware
BOARD_HARDWARE_CLASS := device/samsung/a5lte/cmhw

# Init
#TARGET_UNIFIED_DEVICE := true
#TARGET_INIT_VENDOR_LIB := libinit_msm
#TARGET_LIBINIT_DEFINES_FILE := $(LOCAL_PATH)/init/init_a5lte.c

# FM
TARGET_QCOM_NO_FM_FIRMWARE := true
AUDIO_FEATURE_ENABLED_FM := true

# GPS
#TARGET_GPS_HAL_PATH := $(LOCAL_PATH)/gps
#TARGET_PROVIDES_GPS_LOC_API := true
#BOARD_VENDOR_QCOM_GPS_LOC_API_HARDWARE := $(TARGET_BOARD_PLATFORM)

# Asserts
TARGET_OTA_ASSERT_DEVICE := a5,a5lte,a5ltexx

# Lights
TARGET_PROVIDES_LIBLIGHT := true

# Crypto
TARGET_HW_DISK_ENCRYPTION := true

# Media
TARGET_ENABLE_QC_AV_ENHANCEMENTS := true
TARGET_QCOM_MEDIA_VARIANT := caf

# Partitions
BOARD_FLASH_BLOCK_SIZE := 131072
BOARD_BOOTIMAGE_PARTITION_SIZE := 13631488
BOARD_RECOVERYIMAGE_PARTITION_SIZE := 15728640
BOARD_SYSTEMIMAGE_PARTITION_SIZE := 2411724800
BOARD_PERSISTIMAGE_PARTITION_SIZE := 8388608
BOARD_USERDATAIMAGE_PARTITION_SIZE := 12767444992 

# Qualcomm Hardware
BOARD_USES_QCOM_HARDWARE := true
TARGET_PLATFORM_DEVICE_BASE := /devices/soc.0/
COMMON_GLOBAL_CFLAGS += -DQCOM_BSP
COMMON_GLOBAL_CFLAGS += -DQCOM_HARDWARE
TARGET_USES_QCOM_BSP := true
PROTOBUF_SUPPORTED := true
HAVE_SYNAPTICS_I2C_RMI4_FW_UPGRADE := true

# Power
TARGET_POWERHAL_SET_INTERACTIVE_EXT := $(LOCAL_PATH)/power/power_ext.c
TARGET_POWERHAL_VARIANT := qcom

#charger
BOARD_CHARGER_ENABLE_SUSPEND := true

# Recovery
BOARD_HAS_NO_SELECT_BUTTON := true
BOARD_RECOVERY_SWIPE := true
##BOARD_USE_CUSTOM_RECOVERY_FONT := \"roboto_15x24.h\"
TARGET_RECOVERY_FSTAB := $(LOCAL_PATH)/recovery.fstab
## TARGET_RECOVERY_INITRC := $(LOCAL_PATH)/init.recovery.qcom.rc
TARGET_USERIMAGES_USE_EXT4 := true

# SELINUX
BOARD_SEPOLICY_DIRS := \
	device/samsung/a5lte/sepolicy

BOARD_SEPOLICY_UNION := \
	app.te \
	cpboot-daemon.te \
	domain.te \
	exyrngd.te \
	file.te \
	file_contexts \
	gpsd.te \
	kernel.te \
	lmkd.te \
	init.te \
	macloader.te \
	recovery.te \
	system_server.te \
	rild.te \
	ueventd.te \
	uncrypt.te \
	vold.te \
	wpa.te


# Time services
BOARD_USES_QC_TIME_SERVICES := true

# Vold
TARGET_USE_CUSTOM_LUN_FILE_PATH := /sys/devices/platform/msm_hsusb/gadget/lun%d/file
BOARD_VOLD_DISC_HAS_MULTIPLE_MAJORS := true
BOARD_VOLD_MAX_PARTITIONS := 65

# Wifi
BOARD_HAS_QCOM_WLAN := true
BOARD_HAS_QCOM_WLAN_SDK := true
BOARD_HOSTAPD_DRIVER := NL80211
BOARD_HOSTAPD_PRIVATE_LIB := lib_driver_cmd_qcwcn
BOARD_WLAN_DEVICE := qcwcn
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_qcwcn
TARGET_USES_QCOM_WCNSS_QMI := true
TARGET_USES_WCNSS_CTRL := true
WIFI_DRIVER_FW_PATH_AP := "ap"
WIFI_DRIVER_FW_PATH_STA := "sta"
WPA_SUPPLICANT_VERSION := VER_0_8_X
WIFI_DRIVER_MODULE_PATH := "/system/lib/modules/wlan.ko"
WIFI_DRIVER_MODULE_NAME := "wlan"

# Recovery
#TWRP config
DEVICE_RESOLUTION := 720x1280
RECOVERY_GRAPHICS_USE_LINELENGTH := true
TW_FLASH_FROM_STORAGE := true
HAVE_SELINUX := true
TARGET_RECOVERY_INITRC := device/samsung/a5lte/recovery/init.rc
TW_BRIGHTNESS_PATH := /sys/devices/virtual/lcd/panel/panel/brightness
TW_MAX_BRIGHTNESS := 255
TW_NEW_ION_HEAP := true
TW_TARGET_USES_QCOM_BSP := true
#
BOARD_HAS_NO_SELECT_BUTTON := true
TARGET_RECOVERY_LCD_BACKLIGHT_PATH := \"/sys/devices/virtual/lcd/panel/panel/brightness\"
COMMON_GLOBAL_CFLAGS += -DNO_SECURE_DISCARD
BOARD_CUSTOM_RECOVERY_KEYMAPPING := ../../device/samsung/a5lte/recovery/recovery_keys.c
TARGET_RECOVERY_FSTAB := device/samsung/a5lte/recovery.fstab
RECOVERY_SDCARD_ON_DATA := true
RECOVERY_GRAPHICS_USE_LINELENGTH := true
TW_INTERNAL_STORAGE_PATH := "/data/media"
TW_INTERNAL_STORAGE_MOUNT_POINT := "data"
TW_EXTERNAL_STORAGE_PATH := "/sdcard"
TW_EXTERNAL_STORAGE_MOUNT_POINT := "sdcard"
BOARD_MKRECOVERYIMG_ARGS := --dt device/samsung/a5lte/dt.img
PRODUCT_COPY_FILES += device/samsung/a5lte/twrp.fstab:recovery/root/etc/twrp.fstab
#		      device/samsung/a5lte/rootdir/etc/init:recovery/root/init \
#		      device/samsung/a5lte/rootdir/sbin/e2fsck_static:recovery/root/sbin/e2fsck_static

# inherit from the proprietary version
-include vendor/cyanogen/msm8916-common/BoardConfigVendor.mk

TARGET_TC_ROM := 4.9
TARGET_TC_KERNEL := 4.9
#TARGET_GCC_VERSION_EXP := $(TARGET_TC_ROM)
TARGET_KERNEL_CUSTOM_TOOLCHAIN := $(TARGET_TC_KERNEL)
WITH_LZMA_OTA := true

# Enable dex-preoptimization to speed up first boot sequence
ifeq ($(HOST_OS),linux)
  ifeq ($(TARGET_BUILD_VARIANT),user)
    ifeq ($(WITH_DEXPREOPT),)
      WITH_DEXPREOPT := true
    endif
  endif
endif
