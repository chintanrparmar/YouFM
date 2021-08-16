package app.chintan.youfm.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import app.chintan.youfm.data.State
import app.chintan.youfm.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.Util
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

private const val TAG = "HomeActivity"

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private val playbackStateListener: Player.Listener = playbackStateListener()
    private var player: SimpleExoPlayer? = null

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L
    private val homeViewModel: HomeViewModel by viewModels()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fetchList()
        setUpOnClick()
        setUpObservers()
    }

    private fun fetchList() {
        homeViewModel.getAudioList()
    }

    private fun setUpObservers() {
        homeViewModel.uploadFileLD.observe(this, { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    Toast.makeText(applicationContext, state.data, Toast.LENGTH_SHORT).show()
                }
                is State.Failure -> {
                    Toast.makeText(applicationContext, state.exception.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
        homeViewModel.audioList.observe(this, { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    updateList(state.data)
                }
                is State.Failure -> {
                    Toast.makeText(applicationContext, state.exception.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
        homeViewModel.downloadURL.observe(this, { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    playAudio(state.data)
                }
                is State.Failure -> {
                    Toast.makeText(applicationContext, state.exception.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    private fun playAudio(url: String) {
        releasePlayer()
        initializePlayer(url)
    }

    private fun updateList(audioList: List<StorageReference>) {
        binding.audioListRcv.adapter = HomeListAdapter(audioList) { pathString ->
            homeViewModel.getDownloadURL(pathString)
        }
    }

    private fun showProgressBar() {
    }

    private fun hideProgressBar() {
    }

    private fun setUpOnClick() {
        binding.uploadAudio.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "audio/*"
                resultLauncher.launch(it)
            }
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Uri? = result.data?.data
                homeViewModel.uploadAudio(data, File(data?.path).name)
            }
        }


    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer(playURL: String) {
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        player = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                binding.exoPlayer.player = exoPlayer

                val mediaItem = MediaItem.Builder()
                    .setUri(playURL)
                    .build()
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.prepare()
            }
    }

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
        }
        player = null
    }

}

private fun playbackStateListener() = object : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        val stateString: String = when (playbackState) {
            ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
            ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
            ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
            ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
            else -> "UNKNOWN_STATE             -"
        }
        Log.d(TAG, "changed state to $stateString")
    }
}
