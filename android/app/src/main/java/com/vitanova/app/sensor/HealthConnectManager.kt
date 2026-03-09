package com.vitanova.app.sensor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// ─── Domain objects ────────────────────────────────────────────────────────────

data class HeartRateSample(
    val bpm: Long,
    val timestamp: Instant
)

data class HeartRateData(
    val samples: List<HeartRateSample>,
    val averageBpm: Long,
    val minBpm: Long,
    val maxBpm: Long
)

data class HrvData(
    val rmssdValues: List<Double>,
    val timestamps: List<Instant>,
    val averageRmssd: Double
)

data class StepsData(
    val totalSteps: Long,
    val records: List<StepsRecord>
)

data class DistanceData(
    val totalDistanceMeters: Double,
    val records: List<DistanceRecord>
)

data class CaloriesData(
    val totalCalories: Double,
    val records: List<TotalCaloriesBurnedRecord>
)

data class SleepData(
    val sessions: List<SleepSessionInfo>
)

data class SleepSessionInfo(
    val startTime: Instant,
    val endTime: Instant,
    val duration: Duration,
    val stages: List<SleepStageInfo>
)

data class SleepStageInfo(
    val stage: Int,
    val stageName: String,
    val startTime: Instant,
    val endTime: Instant
)

data class ExerciseData(
    val sessions: List<ExerciseSessionInfo>
)

data class ExerciseSessionInfo(
    val title: String?,
    val exerciseType: Int,
    val startTime: Instant,
    val endTime: Instant,
    val duration: Duration
)

data class OxygenSaturationData(
    val samples: List<OxygenSaturationSample>,
    val averagePercent: Double
)

data class OxygenSaturationSample(
    val percent: Double,
    val timestamp: Instant
)

data class RespiratoryRateData(
    val samples: List<RespiratoryRateSample>,
    val averageRate: Double
)

data class RespiratoryRateSample(
    val breathsPerMinute: Double,
    val timestamp: Instant
)

// ─── Availability & Permission state ───────────────────────────────────────────

enum class HealthConnectAvailability {
    AVAILABLE,
    NOT_INSTALLED,
    NOT_SUPPORTED
}

// ─── Manager ───────────────────────────────────────────────────────────────────

/**
 * Manages reading health & fitness data from Android Health Connect.
 *
 * Health Connect is a platform-level data store that aggregates data from
 * multiple health and fitness apps. This manager:
 * - Detects whether Health Connect is installed and available.
 * - Declares and checks the required read permissions.
 * - Reads heart rate, HRV, steps, distance, calories, sleep, exercise,
 *   oxygen saturation, and respiratory rate records for arbitrary date ranges.
 * - Maps raw Health Connect records to clean domain objects.
 */
class HealthConnectManager(private val context: Context) {

    companion object {
        private const val TAG = "HealthConnectManager"

        private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
        private const val PLAY_STORE_URI =
            "https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE"

        /** All permissions this manager needs to function fully. */
        val REQUIRED_PERMISSIONS = setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(OxygenSaturationRecord::class),
            HealthPermission.getReadPermission(RespiratoryRateRecord::class)
        )
    }

    private var healthConnectClient: HealthConnectClient? = null

    private val _availability = MutableStateFlow(checkAvailability())
    val availability: StateFlow<HealthConnectAvailability> = _availability.asStateFlow()

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    init {
        if (_availability.value == HealthConnectAvailability.AVAILABLE) {
            healthConnectClient = HealthConnectClient.getOrCreate(context)
        }
    }

    // ── Availability & Permissions ─────────────────────────────────────────────

    /**
     * Checks whether Health Connect is installed and usable on this device.
     */
    fun checkAvailability(): HealthConnectAvailability {
        val status = HealthConnectClient.getSdkStatus(context)
        return when (status) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }.also { _availability.value = it }
    }

    /**
     * Returns an [Intent] that opens Health Connect on the Play Store,
     * for cases where it is not installed.
     */
    fun getInstallIntent(): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(PLAY_STORE_URI)
            setPackage("com.android.vending")
        }
    }

    /**
     * Creates the permission-request contract for use with
     * `registerForActivityResult(contract)`.
     */
    fun createPermissionRequestContract() =
        PermissionController.createRequestPermissionResultContract()

    /**
     * Checks which of [REQUIRED_PERMISSIONS] have been granted and updates
     * [permissionsGranted].
     *
     * @return The set of already-granted permissions.
     */
    suspend fun checkPermissions(): Set<String> {
        val client = healthConnectClient ?: return emptySet()
        val granted = client.permissionController.getGrantedPermissions()
        _permissionsGranted.value = granted.containsAll(REQUIRED_PERMISSIONS)
        return granted
    }

    // ── Heart Rate ─────────────────────────────────────────────────────────────

    /**
     * Reads heart rate records for the given time range.
     *
     * @return [HeartRateData] with individual samples and aggregate stats,
     *         or null if Health Connect is unavailable.
     */
    suspend fun readHeartRate(startTime: Instant, endTime: Instant): HeartRateData? {
        val client = healthConnectClient ?: return null

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val samples = response.records.flatMap { record ->
                record.samples.map { sample ->
                    HeartRateSample(
                        bpm = sample.beatsPerMinute,
                        timestamp = sample.time
                    )
                }
            }

            if (samples.isEmpty()) {
                HeartRateData(
                    samples = emptyList(),
                    averageBpm = 0L,
                    minBpm = 0L,
                    maxBpm = 0L
                )
            } else {
                HeartRateData(
                    samples = samples,
                    averageBpm = samples.map { it.bpm }.average().toLong(),
                    minBpm = samples.minOf { it.bpm },
                    maxBpm = samples.maxOf { it.bpm }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read heart rate", e)
            null
        }
    }

    // ── HRV ────────────────────────────────────────────────────────────────────

    /**
     * Reads heart rate variability (RMSSD) records.
     */
    suspend fun readHrv(startTime: Instant, endTime: Instant): HrvData? {
        val client = healthConnectClient ?: return null

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val rmssdValues = response.records.map { it.heartRateVariabilityMillis }
            val timestamps = response.records.map { it.time }

            HrvData(
                rmssdValues = rmssdValues,
                timestamps = timestamps,
                averageRmssd = if (rmssdValues.isNotEmpty()) rmssdValues.average() else 0.0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read HRV", e)
            null
        }
    }

    // ── Steps ──────────────────────────────────────────────────────────────────

    /**
     * Reads step count records and computes the total.
     */
    suspend fun readSteps(startTime: Instant, endTime: Instant): StepsData? {
        val client = healthConnectClient ?: return null

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            StepsData(
                totalSteps = response.records.sumOf { it.count },
                records = response.records
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read steps", e)
            null
        }
    }

    // ── Distance ───────────────────────────────────────────────────────────────

    /**
     * Reads distance records and computes total distance in meters.
     */
    suspend fun readDistance(startTime: Instant, endTime: Instant): DistanceData? {
        val client = healthConnectClient ?: return null

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = DistanceRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            DistanceData(
                totalDistanceMeters = response.records.sumOf { it.distance.inMeters },
                records = response.records
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read distance", e)
            null
        }
    }

    // ── Calories ───────────────────────────────────────────────────────────────

    /**
     * Reads total calories burned records.
     */
    suspend fun readCalories(startTime: Instant, endTime: Instant): CaloriesData? {
        val client = healthConnectClient ?: return null

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            CaloriesData(
                totalCalories = response.records.sumOf { it.energy.inKilocalories },
                records = response.records
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read calories", e)
            null
        }
    }

    // ── Sleep ──────────────────────────────────────────────────────────────────

    /**
     * Reads sleep session records with stage breakdowns.
     */
    suspend fun readSleep(startTime: Instant, endTime: Instant): SleepData? {
        val client = healthConnectClient ?: return null

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val sessions = response.records.map { record ->
                SleepSessionInfo(
                    startTime = record.startTime,
                    endTime = record.endTime,
                    duration = Duration.between(record.startTime, record.endTime),
                    stages = record.stages.map { stage ->
                        SleepStageInfo(
                            stage = stage.stage,
                            stageName = mapSleepStage(stage.stage),
                            startTime = stage.startTime,
                            endTime = stage.endTime
                        )
                    }
                )
            }

            SleepData(sessions = sessions)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read sleep", e)
            null
        }
    }

    // ── Exercise Sessions ──────────────────────────────────────────────────────

    /**
     * Reads exercise/workout session records.
     */
    suspend fun readExerciseSessions(
        startTime: Instant,
        endTime: Instant
    ): ExerciseData? {
        val client = healthConnectClient ?: return null

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val sessions = response.records.map { record ->
                ExerciseSessionInfo(
                    title = record.title,
                    exerciseType = record.exerciseType,
                    startTime = record.startTime,
                    endTime = record.endTime,
                    duration = Duration.between(record.startTime, record.endTime)
                )
            }

            ExerciseData(sessions = sessions)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read exercise sessions", e)
            null
        }
    }

    // ── Oxygen Saturation ──────────────────────────────────────────────────────

    /**
     * Reads blood oxygen saturation (SpO2) records.
     */
    suspend fun readOxygenSaturation(
        startTime: Instant,
        endTime: Instant
    ): OxygenSaturationData? {
        val client = healthConnectClient ?: return null

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = OxygenSaturationRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val samples = response.records.map { record ->
                OxygenSaturationSample(
                    percent = record.percentage.value,
                    timestamp = record.time
                )
            }

            OxygenSaturationData(
                samples = samples,
                averagePercent = if (samples.isNotEmpty()) {
                    samples.map { it.percent }.average()
                } else {
                    0.0
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read oxygen saturation", e)
            null
        }
    }

    // ── Respiratory Rate ───────────────────────────────────────────────────────

    /**
     * Reads respiratory rate records.
     */
    suspend fun readRespiratoryRate(
        startTime: Instant,
        endTime: Instant
    ): RespiratoryRateData? {
        val client = healthConnectClient ?: return null

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RespiratoryRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val samples = response.records.map { record ->
                RespiratoryRateSample(
                    breathsPerMinute = record.rate,
                    timestamp = record.time
                )
            }

            RespiratoryRateData(
                samples = samples,
                averageRate = if (samples.isNotEmpty()) {
                    samples.map { it.breathsPerMinute }.average()
                } else {
                    0.0
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read respiratory rate", e)
            null
        }
    }

    // ── Convenience: read for a single day ─────────────────────────────────────

    /**
     * Reads all supported health data for a single calendar day.
     */
    suspend fun readDaySummary(date: LocalDate): DayHealthSummary? {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant()

        if (healthConnectClient == null) return null

        return try {
            DayHealthSummary(
                date = date,
                heartRate = readHeartRate(start, end),
                hrv = readHrv(start, end),
                steps = readSteps(start, end),
                distance = readDistance(start, end),
                calories = readCalories(start, end),
                sleep = readSleep(start, end),
                exercise = readExerciseSessions(start, end),
                oxygenSaturation = readOxygenSaturation(start, end),
                respiratoryRate = readRespiratoryRate(start, end)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read day summary for $date", e)
            null
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun mapSleepStage(stage: Int): String = when (stage) {
        SleepSessionRecord.STAGE_TYPE_AWAKE -> "Awake"
        SleepSessionRecord.STAGE_TYPE_SLEEPING -> "Sleeping"
        SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> "Out of bed"
        SleepSessionRecord.STAGE_TYPE_LIGHT -> "Light sleep"
        SleepSessionRecord.STAGE_TYPE_DEEP -> "Deep sleep"
        SleepSessionRecord.STAGE_TYPE_REM -> "REM"
        else -> "Unknown ($stage)"
    }
}

/**
 * Aggregated health data for a single calendar day.
 */
data class DayHealthSummary(
    val date: LocalDate,
    val heartRate: HeartRateData?,
    val hrv: HrvData?,
    val steps: StepsData?,
    val distance: DistanceData?,
    val calories: CaloriesData?,
    val sleep: SleepData?,
    val exercise: ExerciseData?,
    val oxygenSaturation: OxygenSaturationData?,
    val respiratoryRate: RespiratoryRateData?
)
