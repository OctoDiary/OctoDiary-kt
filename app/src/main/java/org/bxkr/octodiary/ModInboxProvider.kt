package org.bxkr.octodiary

import org.jivesoftware.smack.packet.XmlEnvironment
import org.jivesoftware.smack.xml.XmlPullParser
import org.jivesoftware.smackx.bytestreams.ibb.packet.DataPacketExtension
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider

class ModInboxProvider : DataPacketProvider.PacketExtensionProvider() {
    override fun parse(
        parser: XmlPullParser?,
        initialDepth: Int,
        xmlEnvironment: XmlEnvironment?
    ): DataPacketExtension {
        return super.parse(parser, initialDepth, xmlEnvironment)
    }
}