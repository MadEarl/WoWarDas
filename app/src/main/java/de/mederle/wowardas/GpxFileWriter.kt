package de.mederle.wowardas

import java.io.File
import java.time.Instant
import java.time.ZoneId

class GpxFileWriter(private val outputFile: File, private val wayPointList: ArrayList<Entry>) {
    private val xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\">"
    private val gpxHeader = "<gpx version=\"1.1\" creator=\"WoWarDas\">"
    private var gpxMetaData: String
    private val gpxFooter = "</gpx>\n"
    private var gpxData: String

    init {
        val metaDataBuilder = StringBuilder()
        metaDataBuilder.append("    <metadata>\n")
        metaDataBuilder.append("        <name>WoWarDas Positionen</name>\n")
        metaDataBuilder.append("        <link href=\"https://github.com/MadEarl/WoWarDas/\">WoWarDas auf GitHub</link>\n")
        metaDataBuilder.append("        <author>Wolfgang Mederle</author>\n")
        metaDataBuilder.append("    </metadata>\n")
        gpxMetaData = metaDataBuilder.toString()
        gpxData = buildGpxFile()
    }

    private fun buildGpxFile(): String {
        val gpxBuilder = StringBuilder()
        gpxBuilder.append(xmlHeader)
        gpxBuilder.append(gpxHeader)
        for (entry in wayPointList) {
            gpxBuilder.append("        <wpt lat=\"${entry.latitude}\" lon=\"${entry.longitude}\">\n")
            gpxBuilder.append("            <time>${Instant.ofEpochSecond(entry.time).atZone(ZoneId.systemDefault()).toLocalDateTime()}</time>\n")
            gpxBuilder.append("            <cmt>${entry.id}</cmt>\n")
            gpxBuilder.append("        </wpt>\n")
        }
        gpxBuilder.append(gpxFooter)
        return gpxBuilder.toString()
    }

    fun writeGpxFile() {

    }
}