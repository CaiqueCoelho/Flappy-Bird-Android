package com.caiquecoelho.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

import sun.rmi.runtime.Log;

public class FlappyBird extends ApplicationAdapter {

	private SpriteBatch batch;
	private Texture[] passaro;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoAlto;
	private Texture gameOver;
	private Random numeroRandomico;
	private BitmapFont fonte;
	private BitmapFont mensagem;
	private Circle passaroCirculo;
	private Rectangle canoAltoRetangulo;
	private Rectangle canoBaixoRetangulo;
	//private ShapeRenderer shape;

	//Atributos de configuracao
    private float larguraDispositivo;
    private float alturaDispositivo;
    private int estadoJogo = 0; //0 -> Jogo não inicidado, 1-> Jogo iniciado, 2-> Jogo Game Over
	private int pontuacao = 0;

	private float variacao = 0;
	private float velocidadeQueda = 0;
	private float posicaoInicialVertical;
	private float posicaoMovimentoCanoHorizontal;
	private float espacoEntreCanos;
	private float deltaTime;
	private float alturaEntreCanosRandomica;
	private boolean marcouPonto = false;

	//Camera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 768;
	private final float VIRTUAL_HEIGHT = 1024;

	@Override
	public void create () {

	    batch = new SpriteBatch();
	    //shape = new ShapeRenderer();
	    numeroRandomico = new Random();
	    passaroCirculo = new Circle();
	    fonte = new BitmapFont();
	    fonte.setColor(Color.WHITE);
	    fonte.getData().setScale(6);

	    mensagem = new BitmapFont();
	    mensagem.setColor(Color.WHITE);
	    mensagem.getData().setScale(3);

	    passaro = new Texture[3];
	    passaro[0] = new Texture("passaro1.png");
        passaro[1] = new Texture("passaro2.png");
        passaro[2] = new Texture("passaro3.png");

        fundo = new Texture("fundo.png");

	    canoBaixo = new Texture("cano_baixo.png");
	    canoAlto  = new Texture("cano_topo.png");

	    gameOver = new Texture("game_over.png");


	    /*
	    * Configuração da câmera
	    * */
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

	    larguraDispositivo = VIRTUAL_WIDTH;
	    alturaDispositivo = VIRTUAL_HEIGHT;

	    posicaoInicialVertical = alturaDispositivo/2;
        posicaoMovimentoCanoHorizontal = larguraDispositivo;
        espacoEntreCanos = 300;

	}

	@Override
	public void render () {

	    camera.update();

        //Limpar frames anteriores
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		deltaTime = Gdx.graphics.getDeltaTime();

		variacao += deltaTime * 7;
		if (variacao > 2) {
			variacao = 0;
		}

		if(estadoJogo == 0) //Nao iniciado
		{
			if(Gdx.input.justTouched()){
				estadoJogo = 1;
			}
		}
		else { //Iniciado

			velocidadeQueda++;
			if (posicaoInicialVertical > 0 || velocidadeQueda < 0) {
				posicaoInicialVertical -= velocidadeQueda;
			}

			if(estadoJogo == 1)
			{

				posicaoMovimentoCanoHorizontal -= deltaTime * 200;

				if (Gdx.input.justTouched()) {
					velocidadeQueda = -10;
				}

				//Verifica se o cano saiu inteiramente da tela
				if (posicaoMovimentoCanoHorizontal < -canoAlto.getWidth()) {
					posicaoMovimentoCanoHorizontal = larguraDispositivo;
					alturaEntreCanosRandomica = numeroRandomico.nextInt(335) - 170;
					marcouPonto = false;
				}

				//Verifica Pontuacao
				if(posicaoMovimentoCanoHorizontal < 120){
					if(!marcouPonto) {
						pontuacao++;
						marcouPonto = true;
					}
				}
			}else{ //Tela de Game Over
				if(Gdx.input.justTouched()){
					estadoJogo = 0;
					pontuacao = 0;
					velocidadeQueda = 0;
					posicaoInicialVertical = alturaDispositivo/2;
					posicaoMovimentoCanoHorizontal = larguraDispositivo;
					marcouPonto = false;
				}


			}

		}

		//Configurar dados de projecao da camera
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

	    batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(canoAlto, posicaoMovimentoCanoHorizontal, alturaDispositivo - canoAlto.getHeight() + espacoEntreCanos / 2 + alturaEntreCanosRandomica);
		batch.draw(canoBaixo, posicaoMovimentoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanosRandomica);
	    batch.draw(passaro[(int)variacao], 120, posicaoInicialVertical);
		fonte.draw(batch, String.valueOf(pontuacao), larguraDispositivo/2, alturaDispositivo - 50);
	    if(estadoJogo == 2){
	    	batch.draw(gameOver, larguraDispositivo/2 - gameOver.getWidth()/2, alturaDispositivo/2 + gameOver.getHeight()/2);
			mensagem.draw(batch, "Toque para Reiniciar", larguraDispositivo/2 - 200, alturaDispositivo/2 - gameOver.getHeight()/2);
	    }
		batch.end();

	    passaroCirculo.set(120 + passaro[0].getWidth()/2, posicaoInicialVertical + passaro[0].getHeight()/2, passaro[0].getWidth()/2);

	    canoBaixoRetangulo = new Rectangle(
			posicaoMovimentoCanoHorizontal,
                alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + alturaEntreCanosRandomica,
				canoBaixo.getWidth(),
				canoBaixo.getHeight()
		);

		canoAltoRetangulo = new Rectangle(
				posicaoMovimentoCanoHorizontal,
                alturaDispositivo - canoAlto.getHeight() + espacoEntreCanos / 2 + alturaEntreCanosRandomica,
				canoAlto.getWidth(),
				canoAlto.getHeight()
		);

	    //Desenhar formas
        /*
		shape.begin(ShapeRenderer.ShapeType.Filled);
		shape.circle(passaroCirculo.x, passaroCirculo.y, passaroCirculo.radius);
		shape.rect(canoBaixoRetangulo.x, canoBaixoRetangulo.y, canoBaixoRetangulo.width, canoBaixoRetangulo.height);
		shape.rect(canoAltoRetangulo.x, canoAltoRetangulo.y, canoAltoRetangulo.width, canoAltoRetangulo.height);
		shape.setColor(Color.RED);
		shape.end();
		*/

		//Teste de colisão
		if(Intersector.overlaps(passaroCirculo, canoBaixoRetangulo) || Intersector.overlaps(passaroCirculo, canoAltoRetangulo)
				|| posicaoInicialVertical <= 0 || posicaoInicialVertical >= alturaDispositivo){
			estadoJogo = 2;
		}

	}

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }
}
