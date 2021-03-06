package net.shadowxcraft.smartlights.packets

import android.util.Log
import net.shadowxcraft.smartlights.ESP32
import net.shadowxcraft.smartlights.PWMDriver

class ReceivedPacketNotificationResponse(controller: ESP32, bytes: ByteArray)
    : ReceivedPacket(controller, bytes)
{
    override fun process() {
        val packetIndex = getInt()
        val success = getByte()
        if (success == 0 || success == 2) {
            controller.unreceivedPackets.remove(packetIndex)
            Log.println(
                Log.INFO, "ReceivedPacketNotifResp",
                "Removing packet ID $packetIndex from queue"
            )
        }
    }
}