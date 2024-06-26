/**
 * 字符串转数组
 * 输入: 000102030405060708
 * @return 返回 {0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08}
 */
fun String.hexToByteArray(): ByteArray {
    var hex = replace(" ", "")
    if (hex.length % 2 != 0) hex = "0${hex}"
    val result = ByteArray(hex.length / 2)
    for (i in result.indices) {
        val index = i * 2
        val hexByte = hex.substring(index, index + 2)
        result[i] = hexByte.toInt(16).toByte()
    }
    return result
}

/**
 * 数组打印
 * @param {0x01,0x02,0x03}
 * @return 01 02 03
 */
fun ByteArray.toHexString(): String = this.toHexString(this.size)
fun ByteArray.toHexString(length: Int): String {
    val sb = StringBuilder()
    val hex =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
    for (i in 0 until length) {
        val value: Int = this[i].toInt() and 0xff
        sb.append(hex[value / 16]).append(hex[value % 16]).append(" ")
    }
    return sb.toString()
}