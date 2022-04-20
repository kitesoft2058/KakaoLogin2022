package com.kitesoft.kakaologin2022

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.kitesoft.kakaologin2022.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val binding:ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var keyHash= Utility.getKeyHash(this)
        Log.i("keyHash", keyHash)

        binding.btn.setOnClickListener { clickKakaoLogin() }
        binding.btnLogout.setOnClickListener { clickKakaoLogout() }
        binding.btnUnlink.setOnClickListener { clickKakaoUnlink() }
    }

    fun clickKakaoLogin(){

        // 카카오톡 로그인을 먼저 시도하고 카카오톡이 설치되어 있지 않을때 카카오 계정으로 로그인하도록 로직 작성...

        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
            else if (token != null) {
                //accessToken 을 통해 사용자 정보를 가져올 수 있음.
                Toast.makeText(this, "로그인 성공 ${token.accessToken}", Toast.LENGTH_SHORT).show()

                // 사용자 정보 요청 (기본)
                loadUserInfo()
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if( UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
            //카카오톡으로 로그인
//            UserApiClient.instance.loginWithKakaoTalk(this, callback = fun(token:OAuthToken?, error:Throwable?){
//            })
            UserApiClient.instance.loginWithKakaoTalk(this){ token, error ->
                if(error!=null){

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)

                }else if (token != null) {
                    //accessToken 을 통해 사용자 정보를 가져올 수 있음.
                    Toast.makeText(this, "카카오 톡 로그인 성공 ${token.accessToken}", Toast.LENGTH_SHORT).show()

                    // 사용자 정보 요청 (기본)
                    loadUserInfo()
                }
            }

        }else{
            // 카카오계정으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback) //default 파라미터는 모두 생략. 대신 파라미터 선택 인자전달.
        }

        // 카카오계정으로 로그인
//        UserApiClient.instance.loginWithKakaoAccount(this,null, null, null, null, null, fun(token:OAuthToken?, error:Throwable?){
//
//        })

        //SAM conversion - callback 익명함수를 제외한 파라미터만 ()에 작성. nullable 파라미터 들이 모두 default value 가 null 로 되어 있어서 생략가능
//        UserApiClient.instance.loginWithKakaoAccount(this){token, error ->
//            if (error != null) {
//                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
//            }
//            else if (token != null) {
//                //accessToken 을 통해 사용자 정보를 가져올 수 있음.
//                Toast.makeText(this, "카카오 계정 로그인 성공 ${token.accessToken}", Toast.LENGTH_SHORT).show()
//
//                // 사용자 정보 요청 (기본)
//                UserApiClient.instance.me { user, error ->
//                    if (error != null) {
//                        Toast.makeText(this, "사용자 정보요청 실패", Toast.LENGTH_SHORT).show()
//                    }
//                    else if (user != null) {
//
//                        val str= "\n회원번호: ${user.id}" +
//                                 "\n이메일: ${user.kakaoAccount?.email}" +
//                                 "\n닉네임: ${user.kakaoAccount?.profile?.nickname}"
//                        binding.tv.text= str
//
//                        Glide.with(this).load(user.kakaoAccount?.profile?.thumbnailImageUrl).into(binding.iv)
//                    }
//                }
//            }
//        }
    }

    fun loadUserInfo(){
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Toast.makeText(this, "사용자 정보요청 실패", Toast.LENGTH_SHORT).show()
            }
            else if (user != null) {

                val str= "\n회원번호: ${user.id}" +
                        "\n이메일: ${user.kakaoAccount?.email}" +
                        "\n닉네임: ${user.kakaoAccount?.profile?.nickname}"
                binding.tv.text= str

                Glide.with(this).load(user.kakaoAccount?.profile?.thumbnailImageUrl).into(binding.iv)
            }
        }
    }

    fun clickKakaoLogout(){
        //로그아웃만 하고 카카오로그인서버와 연결을 끊어진 것이 아니기에 다시 로그인 할때 동의항목을 또 다시 물어보지 않음.
        UserApiClient.instance.logout {error->
            if(error!=null) Toast.makeText(this, "로그아웃 실패 ${error.message}", Toast.LENGTH_SHORT).show()
            else {
                Toast.makeText(this, "로그아웃", Toast.LENGTH_SHORT).show()
                binding.tv.text=""
                Glide.with(this).load("").into(binding.iv)
            }

        }
    }

    fun clickKakaoUnlink(){
        //로그아웃도 되고 카카오로그인서버와 연결이 끊어지기에 다시 로그인할때 동의항목을 다시 물어봄.
        UserApiClient.instance.unlink { error ->
            if(error!=null) Toast.makeText(this, "언링크 실패 ${error.message}", Toast.LENGTH_SHORT).show()
            else {
                Toast.makeText(this, "언링크", Toast.LENGTH_SHORT).show()
                binding.tv.text=""
                Glide.with(this).load("").into(binding.iv)
            }

        }
    }
}