package com.ping.night.story.dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ping.night.story.BuildConfig
import com.ping.night.story.R
import com.ping.night.story.databinding.NsDialogSettingsBinding
import com.ping.night.story.page.NsLanguageActivity
import com.ping.night.story.page.NsTUseActivity

class SettingsDialog(context: Context) : BottomSheetDialog(context, R.style.dialog_bottom_sheet_style){

    private val binding by lazy { NsDialogSettingsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        binding.apply {
            tvAppVersion.text = "V${BuildConfig.VERSION_NAME}"
            settingsPageLanguageLl.setOnClickListener {
                context.startActivity(Intent(context, NsLanguageActivity::class.java))
                dismiss()
            }

            settingsPageGuideLl.setOnClickListener {
                context.startActivity(Intent(context, NsTUseActivity::class.java))
                dismiss()
            }
        }


    }


}