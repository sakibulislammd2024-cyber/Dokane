package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompletedSaleReceipt
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.util.LocalAppLanguage
import com.example.util.ShopLocale
import com.example.util.tr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalReceiptBottomSheet(
    receipt: CompletedSaleReceipt,
    onDismiss: () -> Unit,
    onStartNewSale: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141414),
        scrimColor = Color(0x99000000),
        modifier = Modifier.testTag("digital_receipt_bottom_sheet")
    ) {
        DigitalReceiptContent(
            receipt = receipt,
            onDismiss = onDismiss,
            onStartNewSale = onStartNewSale
        )
    }
}

@Composable
fun DigitalReceiptContent(
    receipt: CompletedSaleReceipt,
    onDismiss: () -> Unit,
    onStartNewSale: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val lang = LocalAppLanguage.current
    val isBaki = receipt.paymentType.equals("baki", ignoreCase = true)

    val formattedDate = remember(receipt.timestamp, lang) {
        ShopLocale.formatDateLong(receipt.timestamp, lang)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success Header Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x265DD62C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = tr("success_cd"),
                        tint = VividGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = tr("sale_completed"),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tr("stock_updated_realtime"),
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("receipt_close_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = tr("close"),
                    tint = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Printable Paper Slip Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0xFF2E2E2E)), RoundedCornerShape(14.dp))
                .testTag("digital_receipt_slip_card"),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1C1C1C)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Store Brand Header
                Text(
                    text = receipt.storeName.ifBlank { tr("receipt_header_store_default") }.uppercase(),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                if (receipt.storePhone.isNotBlank()) {
                    Text(
                        text = tr("phone_prefix", receipt.storePhone),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (receipt.storeAddress.isNotBlank()) {
                    Text(
                        text = receipt.storeAddress,
                        color = TextMuted,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dashed / solid separator
                HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

                Spacer(modifier = Modifier.height(10.dp))

                // Invoice meta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${tr("invoice")}: #${receipt.invoiceNo}",
                            color = VividGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = formattedDate,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isBaki) Color(0x33FFA000) else Color(0x265DD62C)
                    ) {
                        Text(
                            text = if (isBaki) tr("baki_due_badge") else ShopLocale.displayEntryType(receipt.paymentType, lang).uppercase(),
                            color = if (isBaki) Color(0xFFFFB300) else VividGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Customer info if available
                if (receipt.customerName.isNotBlank() && !receipt.customerName.equals("Cash Sale", ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tr("customer_prefix", receipt.customerName) +
                                if (receipt.customerPhone.isNotBlank()) " (${receipt.customerPhone})" else "",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Items Table Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = tr("item_col"),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.8f)
                    )
                    Text(
                        text = tr("qty_col"),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(0.8f)
                    )
                    Text(
                        text = tr("rate_col"),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = tr("amount_col"),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Items rows
                receipt.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.weight(1.8f)
                        )
                        Text(
                            text = ShopLocale.formatNumber(item.qty, lang),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.8f)
                        )
                        Text(
                            text = ShopLocale.formatMoney(item.sellPrice, lang),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = ShopLocale.formatMoneyFixed2(item.lineTotal, lang),
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Subtotal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = tr("subtotal"), color = TextSecondary, fontSize = 12.sp)
                    Text(text = ShopLocale.formatMoneyFixed2(receipt.subtotal, lang), color = TextPrimary, fontSize = 12.sp)
                }

                // Discount (if any)
                if (receipt.discount > 0.0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = tr("total_discount"), color = Color(0xFFFF5252), fontSize = 12.sp)
                        Text(text = "-${ShopLocale.formatMoneyFixed2(receipt.discount, lang)}", color = Color(0xFFFF5252), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Grand Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("grand_total_caps"),
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = ShopLocale.formatMoneyFixed2(receipt.grandTotal, lang),
                        color = VividGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (receipt.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr("note_prefix", receipt.notes),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = tr("thank_you_receipt"),
                    color = TextMuted,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Actions: Share & Copy Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    val shareText = receipt.formatTextReceipt(lang)
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, ShopLocale.get("share_receipt_via", lang))
                    context.startActivity(shareIntent)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("share_receipt_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = tr("share"),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tr("share_receipt"),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(receipt.formatTextReceipt(lang)))
                    Toast.makeText(context, ShopLocale.get("receipt_copied", lang), Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("copy_receipt_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF444444)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = tr("copy"),
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tr("copy_slip"),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Start Next Sale / Done Button
        Button(
            onClick = {
                if (onStartNewSale != null) {
                    onStartNewSale()
                } else {
                    onDismiss()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("next_sale_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = VividGreen
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (onStartNewSale != null) tr("new_sale_next") else tr("done"),
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}
