package dallasgutauckis.scribe.scanner.interfaces

import java.nio.ByteBuffer

interface Scanner<ObjectType> {
    fun scan(
        byteBuffer: ByteBuffer,
        width: Int,
        height: Int,
        rotation: Int
    ): Collection<ObjectType>
}

interface Logger {
    fun log(message: String)
}
