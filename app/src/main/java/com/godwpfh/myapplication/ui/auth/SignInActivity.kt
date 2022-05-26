package com.godwpfh.myapplication.ui.auth

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.godwpfh.myapplication.data.remote.ServiceCreator
import com.godwpfh.myapplication.data.remote.request.RequestSignIn
import com.godwpfh.myapplication.data.remote.response.ResponseSignIn
import com.godwpfh.myapplication.databinding.ActivitySignInBinding
import com.godwpfh.myapplication.ui.home.HomeActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        signUpClicked()
        logInClicked()
        setInfo()

    }

    private fun signUpClicked() {
        binding.buttonSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            activityResultLauncher.launch(intent)

        }
    }

    private fun logInClicked() {

        binding.buttonLogin.setOnClickListener {
            if (binding.edittextId.text.isEmpty() || binding.edittextPw.text.isEmpty()) {
                Toast.makeText(this, "아이디/비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginNetwork()
            //startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    private fun setInfo() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->

                val intent: Intent? = it.data
                val signUpId = intent?.getStringExtra("id").toString()
                val signUpPw = intent?.getStringExtra("pw").toString()
                if (it.resultCode == RESULT_OK) {
                    binding.edittextId.setText(signUpId)
                    binding.edittextPw.setText(signUpPw)
                }
            }
    }

    private fun loginNetwork() {
        val requestSignIn = RequestSignIn(
            email = binding.edittextId.text.toString(),
            password = binding.edittextPw.text.toString()
        )

        val call: Call<ResponseSignIn> = ServiceCreator.soptService.postSignin(requestSignIn)
        call.enqueue(object : Callback<ResponseSignIn> {
            override fun onResponse(
                call: Call<ResponseSignIn>,
                response: Response<ResponseSignIn>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()?.data

                    Toast.makeText(
                        this@SignInActivity,
                        "${data?.email}님 반갑습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@SignInActivity, HomeActivity::class.java)
                    intent.putExtra("username", data!!.email)
                    Log.d(TAG, "SignInActivity - onResponse() called username: ${data!!.email}")
                    startActivity(intent)

                } else if (response.code() == 404) {
                    Toast.makeText(this@SignInActivity, "존재하지 않는 이메일입니다.", Toast.LENGTH_SHORT)
                        .show()
                } else if (response.code() == 409) {
                    Toast.makeText(this@SignInActivity, "잘못돤 비밀번호입니다.", Toast.LENGTH_SHORT).show()
                } else
                    Toast.makeText(this@SignInActivity, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<ResponseSignIn>, t: Throwable) {
                Log.d("NetworkTest", "SignInActivity - onFailure() called, error:$t ")
            }

        })
    }
}