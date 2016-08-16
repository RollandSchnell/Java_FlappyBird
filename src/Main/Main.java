package Main;


import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.opengl.*; 


import Graphics.Shader;
import Input.Input;
import Level.Level;
import Math.Matrix4f;
public class Main implements Runnable {

	
	private int width = 1024;
	private int height = 550;

	private Thread thread;
	private boolean running = false;
	
	private long window;
	
	private Level level;
	
	
	
	//the game's thread
	public void start() {
		running = true;
		thread = new Thread(this, "Game");
		thread.start();
	}
	
	//init the game, along with the openGL methods
	private void init() {
		
		if (glfwInit() != GL_TRUE) {
			System.err.println("Could not init GLFW!");
			return;
		}
		
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
		window = glfwCreateWindow(width, height, "Flappy Bird Game", NULL, NULL);
		if (window == NULL) {
			System.err.println("Could not create GLFW window!");
			return;
		}
		
		/*GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, 50, 50);
		
		*here we could position the window but I did it else how.
		*/
		
		//glfwSetKeyCallback(window, new Input());
		
		Input.init(window);
		//setting the keyboard event handler
		glfwSetKeyCallback(window, Input.getKeyCallback());
		
		//----------- Here init the window display in openGL ---------
		glfwMakeContextCurrent(window);
		glfwShowWindow(window);
		GL.createCapabilities();
		
 		glEnable(GL_DEPTH_TEST);
		glActiveTexture(GL_TEXTURE1);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		//------------------------------------------------------------
		System.out.println("OpenGL: " + glGetString(GL_VERSION));
		Shader.loadAll();
		//creating the game matrix and loading the texture variables
		Matrix4f pr_matrix = Matrix4f.orthographic(-10.0f, 10.0f, -10.0f * 9.0f / 16.0f, 10.0f * 9.0f / 16.0f, -1.0f, 1.0f);
		Shader.BG.setUniformMat4f("pr_matrix", pr_matrix);
		Shader.BG.setUniform1i("tex", 1);
		
		Shader.BIRD.setUniformMat4f("pr_matrix", pr_matrix);
		Shader.BIRD.setUniform1i("tex", 1);
		
		Shader.PIPE.setUniformMat4f("pr_matrix", pr_matrix);
		Shader.PIPE.setUniform1i("tex", 1);
		
		level = new Level();
	
	
	}
	
	public void run() {
		init();
		
		//setting up the game time and also the tick.
		long lastTime = System.nanoTime();
		double delta = 0.0;
		double ns = 1000000000.0 / 60.0;
		long timer = System.currentTimeMillis();
		int updates = 0;
		
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			//getting 60fps/updates
			if (delta >= 1.0) {
				update();
				updates++;
				delta--;
			}
			
			render();
		
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				System.out.println(updates + " fps");
				updates = 0;
				
			}
			if (glfwWindowShouldClose(window) == GL_TRUE)
				running = false;
		}
		
		glfwDestroyWindow(window);
		glfwTerminate();
	}
	
	private void update() {
		
		glfwPollEvents();
		level.update();
	
		if (level.isGameOver()) {
			
			level = new Level();
		}
	}
	
	private void render() {
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); //update the screen
		
		level.render();
		
		int error = glGetError();
		if (error != GL_NO_ERROR)
		{
			System.out.println(error);
		} 
		
		
		glfwSwapBuffers(window);
	}

	public static void main(String[] args) {
		
		new Main().start();
	}
}
