package com.fahomid.securityapp

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Fragment to display and play event clips
class EventClipsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView               // RecyclerView for displaying clips
    private lateinit var playerView: PlayerView                   // PlayerView for video playback
    private lateinit var closeButton: Button                      // Button to close the video player
    private lateinit var videoContainer: FrameLayout              // Container for the video player
    private lateinit var loadingText: TextView                    // TextView to show loading status
    private lateinit var clipsAdapter: ClipsAdapter               // Adapter for the RecyclerView
    private lateinit var clipsApi: ClipsApi                       // API for fetching clips
    private var player: ExoPlayer? = null                         // ExoPlayer for video playback
    private var clipToPlay: String? = null                        // Clip to play when the fragment is created

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            clipToPlay = it.getString("CLIP_NAME")               // Get the clip name from the arguments
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event_clips, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        playerView = view.findViewById(R.id.player_view)
        closeButton = view.findViewById(R.id.close_button)
        videoContainer = view.findViewById(R.id.video_container)
        loadingText = view.findViewById(R.id.loading_text)

        // Retrieve the clip URL from the activity
        val clipsUrl = (activity as DeviceDetailActivity).getClips()
        val baseUrl = clipsUrl?.substring(0, clipsUrl.lastIndexOf('/') + 1)

        // Initialize RecyclerView
        clipsAdapter = ClipsAdapter(emptyList()) { clipPath ->
            Log.d("EventClipsFragment", "Playing video: $clipsUrl/$clipPath")
            playVideo("$clipsUrl/$clipPath")
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = clipsAdapter

        // Initialize Retrofit API
        clipsApi = baseUrl?.let { RetrofitClient.getInstance(it).instance.create(ClipsApi::class.java) }!!

        // Fetch clips from the server
        fetchClips(clipsUrl)

        // Close button listener to stop and reset views
        closeButton.setOnClickListener {
            stopAndResetViews()
        }

        // Handle back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (videoContainer.visibility == View.VISIBLE) {
                    stopAndResetViews()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        return view
    }

    // Fetch the list of clips from the server
    private fun fetchClips(clipsUrl: String) {
        clipsApi.getClips(clipsUrl).enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val clips = response.body() ?: emptyList()
                    Log.d("EventClipsFragment", "Fetched clips: $clips")
                    clipsAdapter.updateClips(clips)

                    // Play the specified clip if provided
                    clipToPlay?.let {
                        if (clips.contains(it)) {
                            playVideo(clipsUrl.replace("/clips", "/$it"))
                        } else {
                            Toast.makeText(requireContext(), "Clip not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("EventClipsFragment", "Failed to fetch clips. Response code: ${response.code()}")
                    Toast.makeText(requireContext(), "Failed to fetch clips", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("EventClipsFragment", "Error fetching clips: ${t.message}", t)
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Play the selected video clip
    private fun playVideo(clipUrl: String) {
        recyclerView.visibility = View.GONE
        videoContainer.visibility = View.VISIBLE
        loadingText.visibility = View.VISIBLE

        // Ensure the player is reset
        player?.release()
        player = SimpleExoPlayer.Builder(requireContext()).build()
        playerView.player = player

        if (!isNetworkAvailable()) {
            Log.e("EventClipsFragment", "Network not available")
            Toast.makeText(requireContext(), "Network not available", Toast.LENGTH_SHORT).show()
            stopAndResetViews()
            return
        }

        Log.d("EventClipsFragment", "Setting video URI: $clipUrl")
        val mediaItem = MediaItem.fromUri(Uri.parse(clipUrl))
        player?.setMediaItem(mediaItem)

        player?.prepare()
        player?.playWhenReady = true
        (player as SimpleExoPlayer)?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        Log.d("EventClipsFragment", "Video prepared")
                        loadingText.visibility = View.GONE
                        playerView.visibility = View.VISIBLE
                    }
                    Player.STATE_ENDED -> {
                        Log.d("EventClipsFragment", "Video playback completed")
                        stopAndResetViews()
                    }
                    Player.STATE_BUFFERING -> {
                        Log.d("EventClipsFragment", "Video buffering")
                        loadingText.visibility = View.VISIBLE
                    }
                    Player.STATE_IDLE -> {
                        Log.d("EventClipsFragment", "Player idle")
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("EventClipsFragment", "Error playing video: ${error.message}")
                Toast.makeText(requireContext(), "Error playing video", Toast.LENGTH_SHORT).show()
                stopAndResetViews()
            }
        })
    }

    // Stop the video player and reset the views
    private fun stopAndResetViews() {
        Log.d("EventClipsFragment", "Stopping and resetting views")
        stopAndResetPlayer()
        videoContainer.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        loadingText.visibility = View.INVISIBLE
    }

    // Release the player and reset it to null
    private fun stopAndResetPlayer() {
        Log.d("EventClipsFragment", "Stopping and resetting player")
        player?.release()
        player = null
    }

    // Check if network is available
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(requireContext(), ConnectivityManager::class.java)
        val network = connectivityManager?.activeNetwork
        val networkCapabilities = connectivityManager?.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Release the player when the view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        stopAndResetPlayer()
    }

    companion object {
        @JvmStatic
        fun newInstance(clipName: String) =
            EventClipsFragment().apply {
                arguments = Bundle().apply {
                    putString("CLIP_NAME", clipName)
                }
            }
    }
}
