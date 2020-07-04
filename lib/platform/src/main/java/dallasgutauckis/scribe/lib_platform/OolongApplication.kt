package dallasgutauckis.scribe.lib_platform

import android.content.Context
import oolong.Render

interface OolongApplication<Msg, Props> {
    fun setRenderer(render: Render<Msg, Props>)

    fun clearRenderer()

    companion object {
        fun <Msg, Props> of(context: Context): OolongApplication<Msg, Props> =
            context.applicationContext as OolongApplication<Msg, Props>
    }
}
