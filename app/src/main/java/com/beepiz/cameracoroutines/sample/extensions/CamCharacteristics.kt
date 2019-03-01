@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.beepiz.cameracoroutines.sample.extensions

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.graphics.Rect
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.BlackLevelPattern
import android.hardware.camera2.params.ColorSpaceTransform
import android.hardware.camera2.params.StreamConfigurationMap
import android.util.Range
import android.util.Rational
import android.util.Size
import android.util.SizeF
import androidx.annotation.RequiresApi
import splitties.systemservices.cameraManager
import android.hardware.camera2.CameraCharacteristics as CC
import android.hardware.camera2.CaptureRequest as CR

@RequiresApi(21)
class CamCharacteristics(private val characteristics: CC) {
    @Throws(CameraAccessException::class)
    constructor(cameraId: String) : this(cameraManager.getCameraCharacteristics(cameraId))

    enum class LensFacing {
        Front,
        Back,
        @TargetApi(23)
        External;

        val intValue: Int
            @SuppressLint("InlinedApi") get() = when (this) {
                Front -> CC.LENS_FACING_FRONT
                Back -> CC.LENS_FACING_BACK
                External -> CC.LENS_FACING_EXTERNAL
            }
    }

    @MustBeDocumented
    annotation class HardwareLevelDependent(val minimumLevel: HardwareLevel)

    enum class HardwareLevel {
        Legacy, Limited, Full
    }

    /**
     * @see CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES
     * @see CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE
     */
    val colorCorrectionAvailableAberrationModes: IntArray get() = characteristics[CC.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES]!!

    val controls = Controls()
    val info = Info()
    val lens = Lens()
    val request = Request()
    @Suppress("SpellCheckingInspection")
    val scaler = Scaler()
    val statisticsInfo = StatisticsInfo()
    val toneMap = ToneMap()

    inner class Controls {
        val autoExposure = AutoExposure()
        val autoFocus = AutoFocus()
        val autoWhiteBalance = AutoWhiteBalance()

        inner class AutoExposure {
            /**
             * @see CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES
             * @see CaptureRequest.CONTROL_AE_ANTIBANDING_MODE
             */
            val availableBandingModes: IntArray get() = characteristics[CC.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES]!!

            /**
             * @see CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES
             * @see CaptureRequest.CONTROL_AE_MODE
             */
            val availableModes: IntArray get() = characteristics[CC.CONTROL_AE_AVAILABLE_MODES]!!

            /**
             * @see CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES
             * @see CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE
             */
            val availableTargetFpsRanges: Array<out Range<Int>> get() = characteristics[CC.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES]!!

            /**
             * @see CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE
             * @see CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION
             */
            val compensationRange: Range<Int> get() = characteristics[CC.CONTROL_AE_COMPENSATION_RANGE]!!

            /**
             * @see CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP
             * @see CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION
             */
            val compensationStep: Rational get() = characteristics[CC.CONTROL_AE_COMPENSATION_STEP]!!

            /**
             * @see CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE
             * @see CaptureRequest.CONTROL_AE_LOCK
             */
            val isLockAvailable: Boolean @RequiresApi(23) get() = characteristics[CC.CONTROL_AE_LOCK_AVAILABLE]!!

            /**
             * @see CameraCharacteristics.CONTROL_MAX_REGIONS_AE
             * @see CaptureRequest.CONTROL_AE_REGIONS
             */
            val maxMeteringRegions: Int get() = characteristics[CC.CONTROL_MAX_REGIONS_AE]!!
        }

        inner class AutoFocus {
            /**
             * @see CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES
             * @see CaptureRequest.CONTROL_AF_MODE
             */
            val availableModes: IntArray get() = characteristics[CC.CONTROL_AF_AVAILABLE_MODES]!!

            /**
             * @see CameraCharacteristics.CONTROL_MAX_REGIONS_AF
             * @see CaptureRequest.CONTROL_AF_REGIONS
             */
            val maxMeteringRegions: Int get() = characteristics[CC.CONTROL_MAX_REGIONS_AF]!!
        }


        /**
         * @see CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS
         * @see CaptureRequest.CONTROL_EFFECT_MODE
         */
        val availableColorEffects: IntArray get() = characteristics[CC.CONTROL_AVAILABLE_EFFECTS]!!

        /**
         * @see CameraCharacteristics.CONTROL_AVAILABLE_MODES
         * @see CaptureRequest.CONTROL_MODE
         */
        val availableModes: IntArray @RequiresApi(23) get() = characteristics[CC.CONTROL_AVAILABLE_MODES]!!

        /**
         * @see CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES
         * @see CaptureRequest.CONTROL_SCENE_MODE
         */
        val availableSceneModes: IntArray get() = characteristics[CC.CONTROL_AVAILABLE_SCENE_MODES]!!

        /**
         * @see CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES
         * @see CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE
         */
        val availableVideoStabilizationModes: IntArray get() = characteristics[CC.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES]!!

        inner class AutoWhiteBalance {
            /**
             * @see CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES
             * @see CaptureRequest.CONTROL_AWB_MODE
             */
            val availableModes: IntArray get() = characteristics[CC.CONTROL_AWB_AVAILABLE_MODES]!!

            /**
             * @see CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE
             * @see CaptureRequest.CONTROL_AWB_LOCK
             */
            val isLockAvailable: Boolean @RequiresApi(23) get() = characteristics[CC.CONTROL_AWB_LOCK_AVAILABLE]!!

            /**
             * @see CameraCharacteristics.CONTROL_MAX_REGIONS_AWB
             * @see CaptureRequest.CONTROL_AWB_REGIONS
             */
            val maxMeteringRegions: Int get() = characteristics[CC.CONTROL_MAX_REGIONS_AWB]!!
        }

        /**
         * @see CameraCharacteristics.CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE
         * @see CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST
         */
        val postRawSensitivityBoostRange: Range<Int>? @RequiresApi(24) get() = characteristics[CC.CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE]
    }

    /**
     * @see CameraCharacteristics.DEPTH_DEPTH_IS_EXCLUSIVE
     */
    @HardwareLevelDependent(HardwareLevel.Limited)
    val isDepthDepthExclusive: Boolean?
        @RequiresApi(23) get() = characteristics[CC.DEPTH_DEPTH_IS_EXCLUSIVE]

    /**
     * @see CameraCharacteristics.DISTORTION_CORRECTION_AVAILABLE_MODES
     * @see CaptureRequest.DISTORTION_CORRECTION_MODE
     */
    val availableDistortionCorrectionModes: IntArray? @RequiresApi(28) get() = characteristics[CC.DISTORTION_CORRECTION_AVAILABLE_MODES]

    /**
     * @see CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES
     * @see CaptureRequest.EDGE_MODE
     */
    val availableEdgeModes: IntArray? get() = characteristics[CC.EDGE_AVAILABLE_EDGE_MODES]

    /**
     * @see CameraCharacteristics.FLASH_INFO_AVAILABLE
     */
    val isFlashInfoAvailable: Boolean get() = characteristics[CC.FLASH_INFO_AVAILABLE]!!

    /**
     * @see CameraCharacteristics.HOT_PIXEL_AVAILABLE_HOT_PIXEL_MODES
     * @see CaptureRequest.HOT_PIXEL_MODE
     */
    val availableHotPixelCorrectionModes: IntArray? get() = characteristics[CC.HOT_PIXEL_AVAILABLE_HOT_PIXEL_MODES]

    inner class Info {
        /**
         * @see CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
         */
        val supportedHardwareLevel: Int get() = characteristics[CC.INFO_SUPPORTED_HARDWARE_LEVEL]!!

        /**
         * @see CameraCharacteristics.INFO_VERSION
         */
        val version: String? @RequiresApi(28) get() = characteristics[CC.INFO_VERSION]
    }

    /**
     * @see CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES
     * @see CaptureRequest.JPEG_THUMBNAIL_SIZE
     */
    val availableJpegThumbnailSize: Array<Size> get() = characteristics[CC.JPEG_AVAILABLE_THUMBNAIL_SIZES]!!

    inner class Lens {
        val info = Info()

        inner class Info {
            /**
             * @see CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES
             * @see CaptureRequest.LENS_APERTURE
             */
            val availableApertureSizes: FloatArray? get() = characteristics[CC.LENS_INFO_AVAILABLE_APERTURES]

            /**
             * @see CameraCharacteristics.LENS_INFO_AVAILABLE_FILTER_DENSITIES
             * @see CaptureRequest.LENS_FILTER_DENSITY
             */
            val availableFilterDensities: FloatArray? get() = characteristics[CC.LENS_INFO_AVAILABLE_FILTER_DENSITIES]

            /**
             * @see CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
             * @see CaptureRequest.LENS_FOCAL_LENGTH
             */
            val availableFocalLengths: FloatArray get() = characteristics[CC.LENS_INFO_AVAILABLE_FOCAL_LENGTHS]!!

            /**
             * @see CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION
             * @see CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE
             */
            val availableOpticalStabilizationModes: IntArray? get() = characteristics[CC.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION]

            /**
             * @see CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION
             * @see CaptureRequest.LENS_FOCUS_DISTANCE
             */
            val focusDistanceCalibrationQuality: Int? get() = characteristics[CC.LENS_INFO_FOCUS_DISTANCE_CALIBRATION]

            /**
             * @see CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE
             * @see focusDistanceCalibrationQuality
             * @see CaptureRequest.LENS_FOCUS_DISTANCE
             */
            @Suppress("SpellCheckingInspection")
            val hyperfocalDistance: Float?
                get() = characteristics[CC.LENS_INFO_HYPERFOCAL_DISTANCE]

            /**
             * @see CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE
             * @see CaptureRequest.LENS_FOCUS_DISTANCE
             */
            val minimumFocusDistance: Float? get() = characteristics[CC.LENS_INFO_MINIMUM_FOCUS_DISTANCE]

        }

        /**
         * @see CameraCharacteristics.LENS_DISTORTION
         */
        val distortionCorrectionCoefficients: FloatArray? @RequiresApi(28) get() = characteristics[CC.LENS_DISTORTION]

        /**
         * @see CameraCharacteristics.LENS_FACING
         */
        val facing: Int get() = characteristics[CC.LENS_FACING]!!

        /**
         * @see CameraCharacteristics.LENS_INTRINSIC_CALIBRATION
         */
        val intrinsicCalibration: FloatArray? @RequiresApi(23) get() = characteristics[CC.LENS_INTRINSIC_CALIBRATION]

        /**
         * @see CameraCharacteristics.LENS_POSE_REFERENCE
         */
        val poseReference: Int? @RequiresApi(28) get() = characteristics[CC.LENS_POSE_REFERENCE]

        /**
         * @see CameraCharacteristics.LENS_POSE_ROTATION
         */
        val poseRotation: FloatArray? @RequiresApi(23) get() = characteristics[CC.LENS_POSE_ROTATION]

        /**
         * @see CameraCharacteristics.LENS_POSE_TRANSLATION
         */
        val poseTranslation: FloatArray? @RequiresApi(23) get() = characteristics[CC.LENS_POSE_TRANSLATION]

        /**
         * @see CameraCharacteristics.LENS_RADIAL_DISTORTION
         */
        @Deprecated("Was deprecated in API 28", ReplaceWith("distortionCorrectionCoefficients"))
        val radialDistortion: FloatArray?
            @RequiresApi(23)
            @Suppress("DEPRECATION")
            get() = characteristics[CC.LENS_RADIAL_DISTORTION]

        /**
         * @see CameraCharacteristics.SHADING_AVAILABLE_MODES
         * @see CaptureRequest.SHADING_MODE
         */
        val availableShadingModes: IntArray @RequiresApi(23) get() = characteristics[CC.SHADING_AVAILABLE_MODES]!!
    }

    /**
     * @see CameraCharacteristics.LOGICAL_MULTI_CAMERA_SENSOR_SYNC_TYPE
     */
    val logicalMultiCameraSensorFrameTimestampSyncAccuracy: Int? @RequiresApi(28) get() = characteristics[CC.LOGICAL_MULTI_CAMERA_SENSOR_SYNC_TYPE]

    /**
     * @see CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES
     * @see CaptureRequest.NOISE_REDUCTION_MODE
     */
    val availableNoiseReductionModes: IntArray? get() = characteristics[CC.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES]

    /**
     * @see CameraCharacteristics.REPROCESS_MAX_CAPTURE_STALL
     */
    val reprocessMaxCaptureStall: Int? @RequiresApi(23) get() = characteristics[CC.REPROCESS_MAX_CAPTURE_STALL]

    inner class Request {

        /**
         * @see CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES
         */
        val availableCapabilities: IntArray get() = characteristics[CC.REQUEST_AVAILABLE_CAPABILITIES]!!

        /**
         * @see CameraCharacteristics.REQUEST_MAX_NUM_INPUT_STREAMS
         */
        val maxInputStreamsNumber: Int? @RequiresApi(23) get() = characteristics[CC.REQUEST_MAX_NUM_INPUT_STREAMS]

        /**
         * @see CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC
         */
        val maxProcessedOutputStreamsNumber: Int get() = characteristics[CC.REQUEST_MAX_NUM_OUTPUT_PROC]!!

        /**
         * @see CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC_STALLING
         */
        val maxProcessedIncludingStallingOutputStreamsNumber: Int get() = characteristics[CC.REQUEST_MAX_NUM_OUTPUT_PROC_STALLING]!!

        /**
         * @see CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_RAW
         */
        val maxRawOutputStreamsNumber: Int get() = characteristics[CC.REQUEST_MAX_NUM_OUTPUT_RAW]!!

        /**
         * @see CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT
         */
        val partialResultCount: Int? get() = characteristics[CC.REQUEST_PARTIAL_RESULT_COUNT]

        /**
         * @see CameraCharacteristics.REQUEST_PIPELINE_MAX_DEPTH
         */
        val pipelineMaxDepth: Byte get() = characteristics[CC.REQUEST_PIPELINE_MAX_DEPTH]!!
    }

    @Suppress("SpellCheckingInspection")
    inner class Scaler {
        /**
         * @see CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM
         * @see CaptureRequest.SCALER_CROP_REGION
         */
        val maxDigitalZoomScaleFactor: Float get() = characteristics[CC.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM]!!

        /**
         * @see CameraCharacteristics.SCALER_CROPPING_TYPE
         * @see CaptureRequest.SCALER_CROP_REGION
         */
        val croppingType: Int get() = characteristics[CC.SCALER_CROPPING_TYPE]!!

        /**
         * @see CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
         */
        val streamConfigurationMap: StreamConfigurationMap get() = characteristics[CC.SCALER_STREAM_CONFIGURATION_MAP]!!
    }

    inner class Sensor {
        val info = Info()

        /**
         * @see CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES
         */
        val availableTestPatternModes: IntArray? get() = characteristics[CC.SENSOR_AVAILABLE_TEST_PATTERN_MODES]

        /**
         * @see CameraCharacteristics.SENSOR_BLACK_LEVEL_PATTERN
         */
        val blackLevelPattern: BlackLevelPattern? get() = characteristics[CC.SENSOR_BLACK_LEVEL_PATTERN]

        /**
         * @see CameraCharacteristics.SENSOR_CALIBRATION_TRANSFORM1
         */
        val calibrationTransform1: ColorSpaceTransform? get() = characteristics[CC.SENSOR_CALIBRATION_TRANSFORM1]

        /**
         * @see CameraCharacteristics.SENSOR_CALIBRATION_TRANSFORM2
         */
        val calibrationTransform2: ColorSpaceTransform? get() = characteristics[CC.SENSOR_CALIBRATION_TRANSFORM2]

        /**
         * @see CameraCharacteristics.SENSOR_COLOR_TRANSFORM1
         */
        val colorTransform1: ColorSpaceTransform? get() = characteristics[CC.SENSOR_COLOR_TRANSFORM1]

        /**
         * @see CameraCharacteristics.SENSOR_COLOR_TRANSFORM2
         */
        val colorTransform2: ColorSpaceTransform? get() = characteristics[CC.SENSOR_COLOR_TRANSFORM2]

        /**
         * @see CameraCharacteristics.SENSOR_FORWARD_MATRIX1
         */
        val forwardMatrix1: ColorSpaceTransform? get() = characteristics[CC.SENSOR_FORWARD_MATRIX1]

        /**
         * @see CameraCharacteristics.SENSOR_FORWARD_MATRIX2
         */
        val forwardMatrix2: ColorSpaceTransform? get() = characteristics[CC.SENSOR_FORWARD_MATRIX2]

        inner class Info {
            /**
             * @see CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE
             */
            val activeArraySize: Rect get() = characteristics[CC.SENSOR_INFO_ACTIVE_ARRAY_SIZE]!!

            /**
             * @see CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT
             */
            val colorFilterArrangement: Int? get() = characteristics[CC.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT]

            /**
             * @see CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE
             */
            val exposureTimeRangeNanos: Range<Long>? get() = characteristics[CC.SENSOR_INFO_EXPOSURE_TIME_RANGE]

            /**
             * @see CameraCharacteristics.SENSOR_INFO_LENS_SHADING_APPLIED
             */
            val isLensShadingCorrectionAppliedOnRawImages: Boolean? @RequiresApi(23) get() = characteristics[CC.SENSOR_INFO_LENS_SHADING_APPLIED]

            /**
             * @see CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION
             */
            val maxFrameDurationNanos: Long? get() = characteristics[CC.SENSOR_INFO_MAX_FRAME_DURATION]

            /**
             * @see CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE
             */
            val pixelArrayPhysicalSizeMillimeters: SizeF get() = characteristics[CC.SENSOR_INFO_PHYSICAL_SIZE]!!

            /**
             * @see CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE
             */
            val pixelArrayDimensions: Size get() = characteristics[CC.SENSOR_INFO_PIXEL_ARRAY_SIZE]!!

            /**
             * @see CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE
             */
            val preCorrectionActivePixelArraySize: Rect @RequiresApi(23) get() = characteristics[CC.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE]!!

            /**
             * @see CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE
             */
            val sensitivitiesRange: Range<Int>? get() = characteristics[CC.SENSOR_INFO_SENSITIVITY_RANGE]

            /**
             * @see CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE
             */
            val timestampSource: Int get() = characteristics[CC.SENSOR_INFO_TIMESTAMP_SOURCE]!!

            /**
             * @see CameraCharacteristics.SENSOR_INFO_WHITE_LEVEL
             */
            val whiteLevel: Int? get() = characteristics[CC.SENSOR_INFO_WHITE_LEVEL]
        }

        /**
         * @see CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY
         */
        val maxAnalogSensitivity: Int? get() = characteristics[CC.SENSOR_MAX_ANALOG_SENSITIVITY]

        /**
         * @see CameraCharacteristics.SENSOR_OPTICAL_BLACK_REGIONS
         */
        val opticalBlackRegions: Array<out Rect>? @RequiresApi(24) get() = characteristics[CC.SENSOR_OPTICAL_BLACK_REGIONS]

        /**
         * @see CameraCharacteristics.SENSOR_ORIENTATION
         */
        val orientation: Int get() = characteristics[CC.SENSOR_ORIENTATION]!!

        /**
         * @see CameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT1
         */
        val standardReferenceIlluminant1: Int? get() = characteristics[CC.SENSOR_REFERENCE_ILLUMINANT1]

        /**
         * @see CameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT2
         */
        val standardReferenceIlluminant2: Byte? get() = characteristics[CC.SENSOR_REFERENCE_ILLUMINANT2]
    }

    inner class StatisticsInfo {
        /**
         * @see CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES
         */
        val availableFaceDetectionModes: IntArray get() = characteristics[CC.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES]!!

        /**
         * @see CameraCharacteristics.STATISTICS_INFO_AVAILABLE_HOT_PIXEL_MAP_MODES
         */
        val availableHotPixelMapOutputModes: BooleanArray? get() = characteristics[CC.STATISTICS_INFO_AVAILABLE_HOT_PIXEL_MAP_MODES]

        /**
         * @see CameraCharacteristics.STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES
         */
        val availableLensShadingMapOutputModes: IntArray? @RequiresApi(23) get() = characteristics[CC.STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES]

        /**
         * @see CameraCharacteristics.STATISTICS_INFO_AVAILABLE_OIS_DATA_MODES
         */
        val availableOisDataOutputModes: IntArray? @RequiresApi(28) get() = characteristics[CC.STATISTICS_INFO_AVAILABLE_OIS_DATA_MODES]

        /**
         * @see CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT
         */
        val maxDetectableFacesCount: Int? get() = characteristics[CC.STATISTICS_INFO_MAX_FACE_COUNT]!!
    }

    /**
     * @see CameraCharacteristics.SYNC_MAX_LATENCY
     */
    val maxSyncLatencyInFrames: Int get() = characteristics[CC.SYNC_MAX_LATENCY]!!

    inner class ToneMap {
        /**
         * @see CameraCharacteristics.TONEMAP_AVAILABLE_TONE_MAP_MODES
         */
        val availableModes: IntArray? get() = characteristics[CC.TONEMAP_AVAILABLE_TONE_MAP_MODES]

        /**
         * @see CameraCharacteristics.TONEMAP_MAX_CURVE_POINTS
         */
        val maxCurvePoints: Int? get() = characteristics[CC.TONEMAP_MAX_CURVE_POINTS]
    }
}
