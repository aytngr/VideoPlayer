package gr.aytn.videoplayer

import android.Manifest.permission.READ_MEDIA_VIDEO
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Size
import android.view.SurfaceView
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import gr.aytn.videoplayer.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), VideoAdapter.OnVideoClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener{
    lateinit var binding: ActivityMainBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var surfaceView: SurfaceView
    private lateinit var uri: Uri
    private val handler: Handler = Handler(Looper.getMainLooper())

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoList = mutableListOf<VideoData>()

        mediaPlayer = MediaPlayer()
        surfaceView = binding.videoPlayer
        initMediaPlayer()

        mediaPlayer.setOnCompletionListener{
            binding.play.setBackgroundResource(R.drawable.ic_play_24)
        }

        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, position: Int, fromUser: Boolean) {
                if(fromUser) mediaPlayer.seekTo(position)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })

        if (ContextCompat.checkSelfPermission(this, READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(READ_MEDIA_VIDEO), 1)
        }

        val resolver = this.contentResolver
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val selection = "${MediaStore.Video.Media.DURATION} <= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES).toString()
        )

        val cursor: Cursor? = resolver.query(
            uri, null, selection, selectionArgs, null
        )
        Toast.makeText(this, "${cursor?.count}", Toast.LENGTH_SHORT).show()
        when {
            cursor == null -> {
                Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
            }
            !cursor.moveToFirst() -> {
                Toast.makeText(this, "no media", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val titleColumn = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val idColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                val albumColumn = cursor.getColumnIndex(MediaStore.Video.Media.ALBUM)
                val durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)

                do {
                    val thisId = cursor.getLong(idColumn)
                    val thisTitle = cursor.getString(titleColumn)
                    val thisAlbum = cursor.getString(albumColumn)
                    val thisDuration = cursor.getInt(durationColumn)
                    val thumbnail = resolver.loadThumbnail(Uri.withAppendedPath(uri, thisId.toString()),
                        Size(200,200),null)
                    if(thisAlbum == "Test") videoList.add(VideoData(thisId, thisTitle, thumbnail,thisDuration))
                } while (cursor.moveToNext())
            }
        }
        cursor?.close()
        val adapter = VideoAdapter(this).apply{
            addData(videoList)
        }
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = adapter

        binding.play.setOnClickListener {
            if(mediaPlayer.isPlaying){
                mediaPlayer.pause()
                binding.play.setBackgroundResource(R.drawable.ic_play_24)
            }else {
                mediaPlayer.start()
                binding.play.setBackgroundResource(R.drawable.ic_pause)
            }
        }

    }

    private fun initMediaPlayer(){
        mediaPlayer.setOnErrorListener(this)
        mediaPlayer.setOnPreparedListener(this)


    }

    private fun startSeekBar(){
        runOnUiThread(object: Runnable {
            override fun run() {
                val currentPosition = mediaPlayer.currentPosition
                binding.seekBar.progress = currentPosition
                handler.postDelayed(this,1000)
            }
        })

    }

    override fun onClick(id: Long, duration: Int) {
        if(mediaPlayer.isPlaying){
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        binding.play.setBackgroundResource(R.drawable.ic_pause)
        binding.seekBar.max = duration

        mediaPlayer.apply {
            setDataSource(applicationContext, Uri.withAppendedPath(uri, id.toString()))
            mediaPlayer.setDisplay(surfaceView.holder)
            prepareAsync()

        }
    }

    override fun onPrepared(p0: MediaPlayer?) {
        mediaPlayer.start()
        startSeekBar()
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        Toast.makeText(this,"error occured",Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.release()
    }

}