import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yushin.flux_lock.databinding.ActivityErrorBinding
import com.yushin.flux_lock.view.BLEActivity

class ErrorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityErrorBinding

    companion object {
        private const val EXTRA_ERROR_TITLE = "EXTRA_ERROR_TITLE"
        private const val EXTRA_ERROR_MESSAGE = "EXTRA_ERROR_MESSAGE"

        fun newIntent(context: Context, errorTitle: String, errorMessage: String): Intent {
            return Intent(context, ErrorActivity::class.java).apply {
                putExtra(EXTRA_ERROR_TITLE, errorTitle)
                putExtra(EXTRA_ERROR_MESSAGE, errorMessage)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // エラータイトルとメッセージを取得して表示
        val errorTitle = intent.getStringExtra(EXTRA_ERROR_TITLE)
        val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE)

        binding.errorTitleTextView.text = errorTitle
        binding.errorMessageTextView.text = errorMessage

        // TOPに戻るボタンのクリックリスナーを設定
        binding.topButton.setOnClickListener {
            val intent = Intent(this, BLEActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // 戻るボタンのクリックリスナーを設定
        binding.backButton.setOnClickListener {
            finish()
        }
    }
}