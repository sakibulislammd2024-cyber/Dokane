package com.example.ui.components

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.InviteCodeConfig
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.util.AppLanguage
import com.example.util.ShopLocale
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Extracts an 8-character store invite code from scanned QR payload using [InviteCodeConfig].
 * Accepts raw codes like "K7M9P2XQ" or text/URL containing an invite code.
 */
fun extractInviteCodeFromQr(rawValue: String): String {
    val len = InviteCodeConfig.CODE_LENGTH
    val trimmed = InviteCodeConfig.normalize(rawValue)
    if (Regex("^[A-Z0-9]{$len}$").matches(trimmed)) {
        return trimmed
    }
    val upperRaw = rawValue.trim().uppercase()
    val strictMatch = Regex("\\b([${InviteCodeConfig.ALLOWED_CHARS}]{$len})\\b").find(upperRaw)
    if (strictMatch != null) {
        return strictMatch.groupValues[1]
    }
    val alphaNumMatch = Regex("\\b([A-Z0-9]{$len})\\b").find(upperRaw)
    return alphaNumMatch?.groupValues?.getOrNull(1) ?: trimmed
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerDialog(
    currentLanguage: AppLanguage,
    onQrScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val hasDeliveredResult = remember { AtomicBoolean(false) }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var hasFlash by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val scanTransition = rememberInfiniteTransition(label = "qr_scan_beam")
    val beamProgress by scanTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beam_y"
    )

    DisposableEffect(lifecycleOwner) {
        var boundProvider: ProcessCameraProvider? = null
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener(
            {
                try {
                    val provider = cameraProviderFuture.get()
                    boundProvider = provider

                    val preview = Preview.Builder().build().apply {
                        surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    val reader = MultiFormatReader().apply {
                        setHints(
                            mapOf(
                                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                                DecodeHintType.TRY_HARDER to true
                            )
                        )
                    }

                    imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        if (hasDeliveredResult.get()) {
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        try {
                            val decoded = decodeQrFromImageProxy(imageProxy, reader)
                            if (!decoded.isNullOrBlank() && hasDeliveredResult.compareAndSet(false, true)) {
                                mainExecutor.execute {
                                    onQrScanned(decoded)
                                }
                            }
                        } finally {
                            imageProxy.close()
                        }
                    }

                    val selector = when {
                        provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                        provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                        else -> null
                    }

                    if (selector == null) {
                        cameraError = ShopLocale.format("no_camera_detected", currentLanguage, InviteCodeConfig.CODE_LENGTH)
                        return@addListener
                    }

                    provider.unbindAll()
                    val boundCamera = provider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageAnalysis
                    )
                    camera = boundCamera
                    hasFlash = boundCamera.cameraInfo.hasFlashUnit()
                } catch (e: Exception) {
                    Log.e("QrScannerDialog", "Failed to start camera", e)
                    cameraError = ShopLocale.format(
                        "unable_open_camera",
                        currentLanguage,
                        e.localizedMessage ?: ShopLocale.get("unknown", currentLanguage)
                    )
                }
            },
            mainExecutor
        )

        onDispose {
            try {
                camera?.cameraControl?.enableTorch(false)
                boundProvider?.unbindAll()
            } catch (e: Exception) {
                Log.w("QrScannerDialog", "Camera cleanup warning", e)
            }
            analysisExecutor.shutdown()
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("qr_scanner_dialog"),
            color = DarkBackground,
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = VividGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ShopLocale.get("scan_owner_qr", currentLanguage),
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasFlash) {
                            IconButton(
                                onClick = {
                                    val nextState = !isTorchEnabled
                                    isTorchEnabled = nextState
                                    camera?.cameraControl?.enableTorch(nextState)
                                },
                                modifier = Modifier.testTag("qr_torch_btn")
                            ) {
                                Icon(
                                    imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = ShopLocale.get(
                                        if (isTorchEnabled) "turn_off_flash" else "turn_on_flash",
                                        currentLanguage
                                    ),
                                    tint = if (isTorchEnabled) VividGreen else TextSecondary
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("qr_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = ShopLocale.get("close_scanner", currentLanguage),
                                tint = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Camera Feed + Viewfinder Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0A0C0E)),
                    contentAlignment = Alignment.Center
                ) {
                    if (cameraError == null) {
                        AndroidView(
                            factory = { previewView },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Dark scrim with transparent square cutout & VividGreen corner reticle
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        ) {
                            val boxSize = size.minDimension * 0.66f
                            val left = (size.width - boxSize) / 2f
                            val top = (size.height - boxSize) / 2f
                            val right = left + boxSize
                            val bottom = top + boxSize

                            // Scrim
                            drawRect(color = Color(0x88000000))

                            // Clear viewfinder hole
                            drawRoundRect(
                                color = Color.Transparent,
                                topLeft = Offset(left, top),
                                size = Size(boxSize, boxSize),
                                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                                blendMode = BlendMode.Clear
                            )

                            // VividGreen corner brackets
                            val strokeW = 4.dp.toPx()
                            val cornerLen = 26.dp.toPx()
                            // Top-Left
                            drawLine(VividGreen, Offset(left, top), Offset(left + cornerLen, top), strokeW, StrokeCap.Round)
                            drawLine(VividGreen, Offset(left, top), Offset(left, top + cornerLen), strokeW, StrokeCap.Round)
                            // Top-Right
                            drawLine(VividGreen, Offset(right, top), Offset(right - cornerLen, top), strokeW, StrokeCap.Round)
                            drawLine(VividGreen, Offset(right, top), Offset(right, top + cornerLen), strokeW, StrokeCap.Round)
                            // Bottom-Left
                            drawLine(VividGreen, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeW, StrokeCap.Round)
                            drawLine(VividGreen, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeW, StrokeCap.Round)
                            // Bottom-Right
                            drawLine(VividGreen, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeW, StrokeCap.Round)
                            drawLine(VividGreen, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeW, StrokeCap.Round)

                            // Animated scan beam
                            val beamY = top + (boxSize * beamProgress)
                            drawLine(
                                color = VividGreen.copy(alpha = 0.85f),
                                start = Offset(left + 14.dp.toPx(), beamY),
                                end = Offset(right - 14.dp.toPx(), beamY),
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22E53935)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = AlertRed,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = cameraError ?: "",
                                color = AlertRed,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = ShopLocale.get("qr_point_camera", currentLanguage),
                    color = TextSecondary,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                GlassOutlinedButton(
                    text = ShopLocale.get("enter_code_manually", currentLanguage),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "qr_manual_fallback_btn"
                )
            }
        }
    }
}

private fun decodeQrFromImageProxy(
    imageProxy: ImageProxy,
    reader: MultiFormatReader
): String? {
    val plane = imageProxy.planes.firstOrNull() ?: return null
    val buffer = plane.buffer
    val data = ByteArray(buffer.remaining())
    buffer.get(data)

    val width = imageProxy.width
    val height = imageProxy.height
    val rowStride = plane.rowStride

    // Normalize Y-plane buffer when rowStride includes padding bytes
    val yData = if (rowStride == width) {
        data
    } else {
        ByteArray(width * height).also { clean ->
            for (row in 0 until height) {
                val srcOffset = row * rowStride
                val copyLen = minOf(width, data.size - srcOffset)
                if (copyLen > 0) {
                    System.arraycopy(data, srcOffset, clean, row * width, copyLen)
                }
            }
        }
    }

    val source = PlanarYUVLuminanceSource(
        yData,
        width,
        height,
        0,
        0,
        width,
        height,
        false
    )
    val bitmap = BinaryBitmap(HybridBinarizer(source))
    return try {
        reader.decodeWithState(bitmap).text
    } catch (_: NotFoundException) {
        null
    } catch (_: Exception) {
        null
    } finally {
        reader.reset()
    }
}
