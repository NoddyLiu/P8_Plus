package com.superh2.p8.serial

import com.superh2.library.myInterface.IPipetteDriver
import com.superh2.p8.MainActivity.Companion.mSerialClientAdp

/**
 * Description: 移液器兼容层
 * Author: liu_ja
 * Created On: 2026/6/9 14:56
 */
object PipetteCompat
{
    val driver: IPipetteDriver by lazy { AdpPipetteDriver(mSerialClientAdp) }
}