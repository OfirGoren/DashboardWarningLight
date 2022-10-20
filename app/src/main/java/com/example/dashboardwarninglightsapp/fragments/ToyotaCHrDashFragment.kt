package com.example.dashboardwarninglightsapp.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dashboardwarninglightsapp.R
import com.example.dashboardwarninglightsapp.databinding.FragmentToyotaCHrDashBinding
import com.example.dashboardwarninglightsapp.objects.FireStoreHandler
import com.example.dashboardwarninglightsapp.objects.WarningLight
import com.example.dashboardwarninglightsapp.utils.SystemServiceUtils
import java.util.*


class ToyotaCHrDashFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentToyotaCHrDashBinding
    private lateinit var zoomIn: Animation
    private lateinit var zoomOut: Animation
    private lateinit var dialogWarningLightBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog
    private var animZoomOutProcess: Boolean? = null
    private var animZoomInProcess: Boolean? = null
    private lateinit var fireStoreHandler: FireStoreHandler
    private lateinit var popUpTxtDetail: AppCompatTextView
    private lateinit var popUpTxtTitle: AppCompatTextView
    private lateinit var popUpImgWarning: AppCompatImageView
    private lateinit var popUpTXTInfo: AppCompatTextView
    private lateinit var popUpTXTInstruction: AppCompatTextView
    private lateinit var popUpPlaySound: AppCompatButton
    private lateinit var popUpTextInfoTitle: AppCompatTextView
    private lateinit var popUpTextInstructionTitle: AppCompatTextView
    private lateinit var popUpInviteGarages: AppCompatButton
    private lateinit var popUpInviteTowing: AppCompatButton
    private lateinit var textSpeech: TextToSpeech
    private lateinit var menuFragment: MenuFragment
    private lateinit var lightNeedsGarages: Array<String>
    private lateinit var lightNeedsTowing: Array<String>

    companion object {
        private const val PLAY_SOUND_TAG = "play"
        private const val STOP_SOUND_TAG = "stop"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentToyotaCHrDashBinding.inflate(inflater, container, false)
        lockOrientationLandScape()
        loadAnim()
        initValues()
        initTextSpeech()
        setTouchListeners()
        setClickListeners()
        createWarningLightDialog()
        initCallBacks()




        return binding.root
    }

    private fun initCallBacks() {
        menuFragment.initBtnCallBack(btnMenuPressedCallBack)
    }

    private fun setClickListeners() {
        binding.toyotaChrTBottomSheet.setOnClickListener(bottomSheetPressed)
    }


    private fun initValues() {
        fireStoreHandler = FireStoreHandler()
        animZoomOutProcess = false
        animZoomInProcess = false
        menuFragment = MenuFragment()
        lightNeedsGarages = WarningLight.getNamesWarningLightGarages()
        lightNeedsTowing = WarningLight.getNamesWarningLightTowing()
    }

    /*
    ------------------------- DashBoard Warning Light ------------------------------------------
    */
    // we use performClick in touch when click on warning light
    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListeners() {
        binding.toyotaChrIMGCheckEngine.setOnTouchListener(touch)
        binding.toyotaChrAbs.setOnTouchListener(touch)
        binding.toyotaChrBrakeUsa.setOnTouchListener(touch)
        binding.toyotaChrBrakeElectric.setOnTouchListener(touch)
        binding.toyotaChrAutoHighBeam.setOnTouchListener(touch)
        binding.toyotaChrHighBeam.setOnTouchListener(touch)
        binding.toyotaChrCruise.setOnTouchListener(touch)
        binding.toyotaChrCurrent.setOnTouchListener(touch)
        binding.toyotaChrBsmOff.setOnTouchListener(touch)
        binding.toyotaChrFogLight.setOnTouchListener(touch)
        binding.toyotaChrFuel.setOnTouchListener(touch)
        binding.toyotaChrGasSide.setOnTouchListener(touch)
        binding.toyotaChrEco.setOnTouchListener(touch)
        binding.toyotaChrHeadLight.setOnTouchListener(touch)
        binding.toyotaChrHighCoolant.setOnTouchListener(touch)
        binding.toyotaChrHold.setOnTouchListener(touch)
        binding.toyotaChrHold2.setOnTouchListener(touch)
        binding.toyotaChrLda.setOnTouchListener(touch)
        binding.toyotaChrParkingBrake.setOnTouchListener(touch)
        binding.toyotaChrPcs.setOnTouchListener(touch)
        binding.toyotaChrRcta.setOnTouchListener(touch)
        binding.toyotaChrSlip.setOnTouchListener(touch)
        binding.toyotaChrVsc.setOnTouchListener(touch)
        binding.toyotaChrTirePressure.setOnTouchListener(touch)
        binding.toyotaChrTemp.setOnTouchListener(touch)
        binding.toyotaChrSteering.setOnTouchListener(touch)
        binding.toyotaChrSrs.setOnTouchListener(touch)
        binding.toyotaChrSport.setOnTouchListener(touch)
        binding.toyotaChrSpeed.setOnTouchListener(touch)
        binding.toyotaChrSeatbelt.setOnTouchListener(touch)
        binding.toyotaChrSet.setOnTouchListener(touch)
    }


    private fun loadAnim() {
        zoomIn = AnimationUtils.loadAnimation(context, R.anim.zoom_in)
        zoomOut = AnimationUtils.loadAnimation(context, R.anim.zoom_out)

    }

    private fun lockOrientationLandScape() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }


    // zoom in when click warning light , zoom out when release warning light
    @SuppressLint("ClickableViewAccessibility")
    private val touch: View.OnTouchListener = View.OnTouchListener { view, event ->

        when (event?.action) {

            MotionEvent.ACTION_DOWN -> {
                view.callOnClick()

                startAnimation(zoomIn, view)
            }
            MotionEvent.ACTION_UP -> {
                view.clearAnimation()
                startAnimation(zoomOut, view)

                val viewName = resources.getResourceEntryName(view.id).replace("_", "")
                popUpImgWarning.setImageResource(
                    this.resources.getIdentifier(
                        view.tag.toString(),
                        "drawable",
                        context?.packageName
                    )
                )

                garagesOrTowingAccordingLight(view.id)
                getWarningLightInfo(viewName)
                view.clearAnimation()


                SystemServiceUtils.getInstance()?.playVibration(60)


            }
        }
        true
    }

    private fun garagesOrTowingAccordingLight(id: Int) {
        val viewName = resources.getResourceEntryName(id)

        if (lightNeedsGarages.contains(viewName)) {
            popUpInviteTowing.visibility = View.INVISIBLE
            popUpInviteGarages.visibility = View.VISIBLE

        } else if (lightNeedsTowing.contains(viewName)) {
            popUpInviteTowing.visibility = View.VISIBLE
            popUpInviteGarages.visibility = View.VISIBLE
        } else {
            popUpInviteTowing.visibility = View.INVISIBLE
            popUpInviteGarages.visibility = View.INVISIBLE
        }

    }


    /*
     ------------------------- DIALOG BUILDER INFO WARNING ----------------------------------
     */
    private fun getWarningLightInfo(viewName: String) {
        fireStoreHandler.getInfoWarningLight(
            viewName,
            object : FireStoreHandler.GetWarningLightInfo {
                override fun isSucceeded(warningLightInfo: WarningLight) {

                    requireActivity().runOnUiThread {

                        popUpTxtTitle.text = warningLightInfo.title
                        popUpTXTInfo.text = warningLightInfo.info
                        popUpTXTInstruction.text = warningLightInfo.instruction

                        showWarningLightDialog()

                    }
                }

                override fun isFailed(exception: Exception) {
                    TODO("Not yet implemented")
                }

            })

    }

    private fun startAnimation(anim: Animation, warningLight: View) {
        warningLight.startAnimation(anim)

    }


    private fun createWarningLightDialog() {
        dialogWarningLightBuilder = AlertDialog.Builder(context, R.style.AlertDialog)
        val contactPopupView = layoutInflater.inflate(R.layout.info_warning_light_pop_up, null)
        dialogWarningLightBuilder.setView(contactPopupView)
        dialog = dialogWarningLightBuilder.create()

        findViewsIdAlertDialog(contactPopupView)
        initListeners()

    }

    private fun initListeners() {
        dialog.setOnCancelListener(cancelDialogPressed)
        popUpPlaySound.setOnClickListener(playSoundPressed)
        popUpInviteGarages.setOnClickListener(inviteGaragesPressed)
        popUpInviteTowing.setOnClickListener(inviteTowingPressed)
    }


    private val cancelDialogPressed = DialogInterface.OnCancelListener {
        textSpeech.stop()
        popUpPlaySound.tag = PLAY_SOUND_TAG
        popUpPlaySound.setBackgroundResource(R.drawable.play_button)

    }

    private fun findViewsIdAlertDialog(contactPopupView: View?) {
        contactPopupView?.let { view ->
            popUpTxtTitle = view.findViewById(R.id.pop_up_TXT_title)
            popUpImgWarning = view.findViewById(R.id.pop_up_IMG_warning)
            popUpTXTInfo = view.findViewById(R.id.pop_up_TXT_info)
            popUpTXTInstruction = view.findViewById(R.id.pop_up_TXT_instruction)
            popUpPlaySound = view.findViewById(R.id.pop_up_play_sound)
            popUpTextInfoTitle = view.findViewById(R.id.pop_up_TXT_info_title)
            popUpTextInstructionTitle = view.findViewById(R.id.pop_up_TXT_instruction_title)
            popUpInviteGarages = view.findViewById(R.id.pop_up_inviteGarages)
            popUpInviteTowing = view.findViewById(R.id.pop_up_inviteTowing)


        }
    }

    private fun showWarningLightDialog() {
        dialog.show()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        dialog.window?.setLayout(width, height)
    }


    private val inviteGaragesPressed = View.OnClickListener {
        findNavController().navigate(R.id.action_toyota_c_hr_dash_fragment2_to_mapFragment)
        dialog.dismiss()

    }

    private val inviteTowingPressed = View.OnClickListener {
        findNavController().navigate(R.id.action_toyota_c_hr_dash_fragment2_to_inviteTowingFragment)
        dialog.dismiss()
    }

    private val playSoundPressed = View.OnClickListener {


        val viewTag = popUpPlaySound.tag
        if (viewTag == STOP_SOUND_TAG) {
            textSpeech.stop()
            popUpPlaySound.setBackgroundResource(R.drawable.play_button)
            popUpPlaySound.tag = PLAY_SOUND_TAG

        } else if (viewTag == PLAY_SOUND_TAG) {
            popUpPlaySound.tag = STOP_SOUND_TAG
            popUpPlaySound.setBackgroundResource(R.drawable.stop)

            val warningText =
                popUpTxtTitle.text.toString() +
                        "\n" + popUpTextInfoTitle.text +
                        "\n" + popUpTXTInfo.text.toString() +
                        "\n" + popUpTextInstructionTitle.text +
                        "\n" + popUpTXTInstruction.text.toString()

            textSpeech.speak(warningText, TextToSpeech.QUEUE_FLUSH, null, "0");
        }


    }


    private fun initTextSpeech() {
        textSpeech = TextToSpeech(context?.applicationContext) { status ->
            if (status != TextToSpeech.ERROR) {
                textSpeech.language = Locale.US

                textSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        activity?.runOnUiThread {
                            popUpPlaySound.setBackgroundResource(R.drawable.stop)
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        activity?.runOnUiThread {
                            popUpPlaySound.setBackgroundResource(R.drawable.play_button)
                        }
                    }

                    override fun onError(utteranceId: String?) {
                    }
                })
            }


        }
    }


    /*
 ------------------------------------------Bottom Sheet----------------------------------------------
  */

    var bottomSheetPressed = View.OnClickListener {
        showBottomSheetDialog()


    }

    @SuppressLint("InflateParams")
    private fun showBottomSheetDialog() {
        menuFragment.show(parentFragmentManager, menuFragment.tag)


    }


    private val btnMenuPressedCallBack = object : MenuFragment.BtnPressedCallBack {
        override fun btnPressedFromBottomSheet(btnId: Int) {


            when (btnId) {
                R.id.nearby_garages -> {

                    findNavController().navigate(R.id.action_toyota_c_hr_dash_fragment2_to_mapFragment)
                }
                R.id.profile -> {

                    findNavController().navigate(R.id.action_toyota_c_hr_dash_fragment2_to_profileFragment)
                }
                R.id.towing -> {
                    findNavController().navigate(R.id.action_toyota_c_hr_dash_fragment2_to_inviteTowingFragment)
                }
            }


        }

    }

    /*
    ----------------------------------------------------------------------------------------------
     */
    override fun onClick(v: View?) {

    }


    override fun onPause() {
        textSpeech.stop()
        textSpeech.shutdown()
        super.onPause()
    }
}




