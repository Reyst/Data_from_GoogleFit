package gsihome.reyst.gfit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private val btnRead: Button by lazy { btn_read }
    private val tvData: TextView by lazy { tv_data }

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_READ)
        .build()

    private val account: GoogleSignInAccount by lazy { GoogleSignIn.getAccountForExtension(this, fitnessOptions) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRead.setOnClickListener { readDataFromGoogleFit() }

    }

    private fun readDataFromGoogleFit() {

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this, // your activity
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                account,
                fitnessOptions);
        } else {
            accessGoogleFit();
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                accessGoogleFit()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun accessGoogleFit() {
        val cal: Calendar = Calendar.getInstance()
        val endTime: Long = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, -1)
        val startTime: Long = cal.timeInMillis

        val readRequest = DataReadRequest.Builder()
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .read(DataType.TYPE_WEIGHT)
            .read(DataType.TYPE_HEIGHT)
            .build()

        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        Fitness.getHistoryClient(this, account)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                tvData.text =
                    "weight: ${response.getDataSet(DataType.TYPE_WEIGHT).dataPoints.firstOrNull()?.getValue(Field.FIELD_WEIGHT)?.asFloat() ?: -1},\n" +
                        "height: ${response.getDataSet(DataType.TYPE_HEIGHT).dataPoints.firstOrNull()?.getValue(Field.FIELD_HEIGHT)?.asFloat() ?: -1}"
                Log.wtf("INSPECT", response.toString())
            }
            .addOnFailureListener { e -> Log.wtf("INSPECT", "OnFailure()", e) }
    }

    companion object {
        private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1
    }
}