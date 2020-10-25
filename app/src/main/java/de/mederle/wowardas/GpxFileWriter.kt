package de.mederle.wowardas

import java.time.Instant
import java.time.ZoneId

class GpxFileWriter(private val wayPointList: MutableList<Entry>) {
    private val xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
    private val gpxHeader = "<gpx version=\"1.1\" creator=\"WoWarDas\"\n" +
            "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "  xmlns=\"http://www.topografix.com/GPX/1/0\"\n" +
            "  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0\n" +
            "  http://www.topografix.com/GPX/1/0/gpx.xsd\">\n"
    private var gpxMetaData: String
    private val gpxFooter = "</gpx>\n"
    private var gpxData: String

    init {
        val metaDataBuilder = StringBuilder()
        metaDataBuilder.append("    <metadata>\n")
        metaDataBuilder.append("        <name>WoWarDas Positionen</name>\n")
        metaDataBuilder.append("        <author>\n")
        metaDataBuilder.append("            <name>Wolfgang Mederle</name>\n")
        metaDataBuilder.append("            <email id=\"jungleoutthere\" domain=\"mederle.de\" />\n")
        metaDataBuilder.append("            <link href=\"https://github.com/MadEarl/WoWarDas/\"><text>WoWarDas auf GitHub</text></link>\n")
        metaDataBuilder.append("        </author>\n")
        metaDataBuilder.append("    </metadata>\n")
        gpxMetaData = metaDataBuilder.toString()
        gpxData = buildGpxFile()
    }

    fun getContents(): String {
        return gpxData
    }

    private fun buildGpxFile(): String {
        val gpxBuilder = StringBuilder()
        gpxBuilder.append(xmlHeader)
        gpxBuilder.append(gpxHeader)
        gpxBuilder.append(gpxMetaData)
        for (entry in wayPointList) {
            gpxBuilder.append("        <wpt lat=\"${entry.latitude}\" lon=\"${entry.longitude}\">\n")
            gpxBuilder.append(
                "            <time>${
                    Instant.ofEpochSecond(entry.time).atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                }</time>\n"
            )
            gpxBuilder.append("            <cmt>${entry.id}</cmt>\n")
            gpxBuilder.append("        </wpt>\n")
        }
        gpxBuilder.append(gpxFooter)
        return gpxBuilder.toString()
    }
}