package com.example.brick_breaker

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.opengl.Visibility
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var scoreText: TextView
    private lateinit var paddle: View
    private lateinit var ball: View
    private lateinit var brickContainer: LinearLayout

    private var ballX = 0f
    private var ballY = 0f
    private var ballSpeedX = 0f

    private var ballSpeedY = 0f

    private var paddleX = 0f

    private var score = 0


    private val brickRows = 9

    private val brickColumns = 10
    private val brickWidth = 100
    private val brickHeight = 40
    private val brickMargin = 4

    private var lives = 3

    private var bricksInitialized = false
    private var gameOver = false



    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scoreText = findViewById(R.id.scoreText)
        paddle = findViewById(R.id.paddle)
        ball = findViewById(R.id.ball)
        brickContainer = findViewById(R.id.brickContainer)

        val newgame = findViewById<Button>(R.id.newgame)

        newgame.setOnClickListener {
            resetGame()
            newgame.visibility = View.INVISIBLE
        }
    }

    private fun resetGame() {
        gameOver = false  // Reinicia la bandera del estado del juego
        lives = 3
        score = 0
        scoreText.text = "Puntaje: $score"
        resetBallPosition()
        initializeBricks()
        start()
    }

    private fun initializeBricks() {
        if (bricksInitialized) {
            // Si los ladrillos ya están inicializados, simplemente restablecemos su visibilidad
            for (row in 0 until brickRows) {
                val rowLayout = brickContainer.getChildAt(row) as LinearLayout

                for (col in 0 until brickColumns) {
                    val brick = rowLayout.getChildAt(col) as View
                    brick.visibility = View.VISIBLE
                }
            }
        } else {
            // Si es la primera vez que se inicializan los ladrillos, creamos la cuadrícula normalmente
            for (row in 0 until brickRows) {
                val rowLayout = LinearLayout(this)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                rowLayout.layoutParams = params

                for (col in 0 until brickColumns) {
                    val brick = View(this)
                    val brickParams = LinearLayout.LayoutParams(brickWidth, brickHeight)
                    brickParams.setMargins(brickMargin, brickMargin, brickMargin, brickMargin)
                    brick.layoutParams = brickParams
                    brick.setBackgroundResource(R.drawable.ic_launcher_background)
                    rowLayout.addView(brick)
                }

                brickContainer.addView(rowLayout)
            }

            bricksInitialized = true
        }
    }

    private fun moveBall() {
        ballX += ballSpeedX
        ballY += ballSpeedY

        ball.x = ballX
        ball.y = ballY
    }

    private fun movePaddle(x: Float) {
        paddleX = x - paddle.width / 2
        paddle.x = paddleX
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun checkCollision() {
        // Aca comprobamos colision con las paredes
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        if (ballX <= 0 || ballX + ball.width >= screenWidth) {
            ballSpeedX *= -1
        }

        if (ballY <= 0) {
            ballSpeedY *= -1
        }

        // Aca comprobamos la colision con la paleta
        if (ballY + ball.height >= paddle.y && ballY + ball.height <= paddle.y + paddle.height
            && ballX + ball.width >= paddle.x && ballX <= paddle.x + paddle.width
        ) {
            ballSpeedY *= -1
            score++
            scoreText.text = "Puntaje: $score"
        }

        if (ballY + ball.height >= screenHeight) {
            resetBallPosition() // Restablecemos la posicion inicial de la bola
        }

        var allBricksDestroyed = true  // Variable para verificar si todos los bloques han sido destruidos
        for (row in 0 until brickRows) {
            val rowLayout = brickContainer.getChildAt(row) as LinearLayout

            val rowTop = rowLayout.y + brickContainer.y

            for (col in 0 until brickColumns) {
                val brick = rowLayout.getChildAt(col) as View

                if (brick.visibility == View.VISIBLE) {
                    allBricksDestroyed = false  // Hay al menos un bloque visible, no todos han sido destruidos

                    val brickLeft = brick.x + rowLayout.x
                    val brickRight = brickLeft + brick.width
                    val brickTop = brick.y + rowTop
                    val brickBottom = brickTop + brick.height

                    if (ballX + ball.width >= brickLeft && ballX <= brickRight
                        && ballY + ball.height >= brickTop && ballY <= brickBottom
                    ) {
                        brick.visibility = View.INVISIBLE
                        ballSpeedY *= -1
                        score++
                        scoreText.text = "Puntaje: $score"
                        // No hay necesidad de retornar aquí
                    }
                }
            }
        }

        // Verificar si todos los bloques han sido destruidos
        if (allBricksDestroyed) {
            // Todos los bloques han sido destruidos, mostrar mensaje de ganador
            showWinnerMessage()
        }

        // Comprobamos colisión con la pared inferior
        if (ballY + ball.height >= screenHeight - 100 && !gameOver) {
            // Reduce el numero de vidas
            lives--

            if (lives > 0 ) {
                Toast.makeText(this, "Te quedan $lives vidas ", Toast.LENGTH_SHORT).show()
            }

            paddle.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        movePaddle(event.rawX)
                    }
                }
                true
            }

            if (lives <= 0) {
                gameOver = true
                gameOver()
            } else {
                // Restablecemos la pelota a su posición inicial
                resetBallPosition()
                start()
            }
        }

    }

    private fun resetBallPosition() {
        if (!gameOver) {  // Evita respawnear la bola si el juego ha terminado
            // Restablece la posición de la pelota a su posición inicial
            val displayMetrics = resources.displayMetrics
            val screenDensity = displayMetrics.density

            val screenWidth = displayMetrics.widthPixels.toFloat()
            val screenHeight = displayMetrics.heightPixels.toFloat()

            ballX = screenWidth / 2 - ball.width / 2
            ballY = screenHeight / 2 - ball.height / 2 + 525

            ball.x = ballX
            ball.y = ballY

            // Restablece la velocidad de la pelota a valores más bajos
            ballSpeedX = 1 * screenDensity
            ballSpeedY = -1 * screenDensity

            paddleX = screenWidth / 2 - paddle.width / 2
            paddle.x = paddleX
        }
    }

    private fun gameOver() {
        // Aca mostramos un mensaje de finalización del juego
        scoreText.text = "Game Over"
        val newgame = findViewById<Button>(R.id.newgame)
        newgame.visibility = View.VISIBLE
    }

    private fun showWinnerMessage() {
        // Mostrar mensaje de ganador
        scoreText.text = "¡Eres un ganador!"
        val newgame = findViewById<Button>(R.id.newgame)
        newgame.visibility = View.VISIBLE
        gameOver = true  // Establecer el estado del juego a "game over"
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun movepaddle() {

        paddle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    movePaddle(event.rawX)
                }
            }
            true
        }
    }


    private fun start() {
        if (!gameOver) {  // Evita iniciar la animación si el juego ha terminado
            movepaddle()
            val displayMetrics = resources.displayMetrics
            val screenDensity = displayMetrics.density

            val screenWidth = displayMetrics.widthPixels.toFloat()
            val screenHeight = displayMetrics.heightPixels.toFloat()

            paddleX = screenWidth / 2 - paddle.width / 2
            paddle.x = paddleX

            ballX = screenWidth / 2 - ball.width / 2
            ballY = screenHeight / 2 - ball.height / 2

            val brickHeightWithMargin = (brickHeight + brickMargin * screenDensity).toInt()

            ballSpeedX = 3 * screenDensity
            ballSpeedY = -3 * screenDensity

            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = Long.MAX_VALUE
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener { animation ->
                moveBall()
                checkCollision()
            }
            animator.start()
        }
    }

}