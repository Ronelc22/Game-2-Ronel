package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Motor de Chat de Voz por Proximidad y Audio Espacial 3D.
 * Gestiona captura de micrófono real, cálculo de amplitud RMS,
 * atenuación por distancia y paneo estéreo.
 */
class VoiceChatEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var recordingJob: Job? = null
    private var audioRecord: AudioRecord? = null

    // Flujos de estado observables para la UI
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _micAmplitude = MutableStateFlow(0f)
    val micAmplitude: StateFlow<Float> = _micAmplitude.asStateFlow()

    private val _isUserSpeaking = MutableStateFlow(false)
    val isUserSpeaking: StateFlow<Boolean> = _isUserSpeaking.asStateFlow()

    // Configuraciones de voz
    var isMuted: Boolean = false
    var isDeafened: Boolean = false
    var isPushToTalk: Boolean = false
    var isPttPressed: Boolean = false
    var voiceThreshold: Float = 0.12f
    var maxHearingDistance: Float = 18.0f // Metros en el mundo

    // Sintetizador de audio espacial para compañeros/actividades
    private var companionAudioTrack: AudioTrack? = null
    private var isCompanionAudioActive = false

    private val sampleRate = 22050
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(2048)

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startListening() {
        if (!hasRecordPermission()) return
        if (recordingJob?.isActive == true) return

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            _isRecording.value = true

            recordingJob = scope.launch {
                val buffer = ShortArray(bufferSize / 2)
                var smoothedAmp = 0f

                while (isActive) {
                    if (isMuted || (isPushToTalk && !isPttPressed)) {
                        _micAmplitude.value = 0f
                        _isUserSpeaking.value = false
                        kotlinx.coroutines.delay(50)
                        continue
                    }

                    val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readCount > 0) {
                        var sumSquared = 0.0
                        for (i in 0 until readCount) {
                            val sample = buffer[i].toDouble() / 32768.0
                            sumSquared += sample * sample
                        }
                        val rms = sqrt(sumSquared / readCount).toFloat()
                        // Suavizado dinámico
                        smoothedAmp = smoothedAmp * 0.7f + rms * 0.3f
                        _micAmplitude.value = smoothedAmp.coerceIn(0f, 1f)
                        _isUserSpeaking.value = smoothedAmp > voiceThreshold
                    }
                    kotlinx.coroutines.delay(30)
                }
            }
        } catch (e: Exception) {
            _isRecording.value = false
        }
    }

    fun stopListening() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        _isRecording.value = false
        _micAmplitude.value = 0f
        _isUserSpeaking.value = false
    }

    /**
     * Calcula la atenuación de volumen y el paneo estéreo respecto a la cámara/oyente.
     * Retorna Pair(volumenIzquierdo, volumenDerecho).
     */
    fun calculateSpatialGain(
        listenerX: Float,
        listenerY: Float,
        listenerHeading: Float,
        sourceX: Float,
        sourceY: Float
    ): Pair<Float, Float> {
        if (isDeafened) return Pair(0f, 0f)

        val dx = sourceX - listenerX
        val dy = sourceY - listenerY
        val dist = hypot(dx, dy)

        if (dist > maxHearingDistance) return Pair(0f, 0f)

        // Caída cuadrática suave de volumen con la distancia
        val distNorm = (dist / maxHearingDistance).coerceIn(0f, 1f)
        val baseVolume = (1f - distNorm) * (1f - distNorm)

        // Paneo estéreo basado en el ángulo relativo
        val worldAngle = atan2(dy, dx)
        val relativeAngle = worldAngle - listenerHeading
        val pan = sin(relativeAngle).coerceIn(-1f, 1f) // -1 izquierda, +1 derecha

        val leftGain = (baseVolume * ((1f - pan) * 0.5f + 0.25f)).coerceIn(0f, 1f)
        val rightGain = (baseVolume * ((1f + pan) * 0.5f + 0.25f)).coerceIn(0f, 1f)

        return Pair(leftGain, rightGain)
    }

    /**
     * Reproduce una melodía de guitarra acústica o murmullo de voz espacial
     * cuando un amigo habla o se toca música en la fogata.
     */
    fun playSpatialTone(
        frequencies: List<Float>,
        durationMs: Int,
        leftGain: Float,
        rightGain: Float
    ) {
        if (leftGain <= 0.001f && rightGain <= 0.001f) return

        scope.launch {
            try {
                val trackSampleRate = 22050
                val totalSamples = (trackSampleRate * (durationMs / 1000f)).toInt()
                val audioBuffer = ShortArray(totalSamples * 2) // Stereo (L, R)

                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / trackSampleRate
                    var sampleVal = 0.0
                    for (freq in frequencies) {
                        sampleVal += sin(2.0 * PI * freq * t)
                    }
                    sampleVal /= frequencies.size.coerceAtLeast(1)

                    // Envolvente de decaimiento natural
                    val env = (1.0 - (i.toDouble() / totalSamples)).coerceIn(0.0, 1.0)
                    val rawSample = (sampleVal * env * 32000.0).toInt().coerceIn(-32767, 32767)

                    val leftSample = (rawSample * leftGain).toInt().toShort()
                    val rightSample = (rawSample * rightGain).toInt().toShort()

                    audioBuffer[i * 2] = leftSample
                    audioBuffer[i * 2 + 1] = rightSample
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(trackSampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build()
                    )
                    .setBufferSizeInBytes(audioBuffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(audioBuffer, 0, audioBuffer.size)
                track.play()
                kotlinx.coroutines.delay(durationMs.toLong() + 50)
                track.release()
            } catch (_: Exception) {}
        }
    }

    fun release() {
        stopListening()
        companionAudioTrack?.release()
        companionAudioTrack = null
    }
}
