package com.kitesoft.kakaologin2022

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Kakao SDK 초기화
        KakaoSdk.init(this, "28b5c6df6b9e362b48910b3920cd5575")
    }
}