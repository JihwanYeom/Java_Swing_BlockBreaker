import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

class ParticlePos {
	int x, y;
	ParticlePos() {
		x = (int)(Math.random()*800);
		y = (int)(Math.random()*800);
	}
}

class Background {
	static ArrayList<ParticlePos> points;
	Background() {
		points = new ArrayList<ParticlePos>();
		for (int i = 0; i < 100; i++) {
			points.add(new ParticlePos());
		}
	}
	
	static void draw(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		
		GradientPaint gradient = new GradientPaint(0, 0, new Color(0, 0, 20), 0, 800, new Color(50, 50, 100));
        g2.setPaint(gradient);
        g2.fillRect(0, 0, 800, 800);
        
        for(var p : points ) {
        	g2.setColor(new Color(255,255,255));
        	g2.fillRect(p.x, p.y, 5, 5);
        }
	}
}

class Audio {
	static Clip clip;
	Audio(){
		
	}
	static void playAudio(String audio) {
		try {
			if(clip != null)
				clip.stop();
			clip = AudioSystem.getClip();
			ClassLoader classLoader = Audio.class.getClassLoader(); // Audio 클래스의 클래스 로더 사용
            URL url = classLoader.getResource(audio);
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
			clip.open(audioStream);
		} catch(Exception e) {
			e.printStackTrace();
		}
		clip.setFramePosition(0);
		clip.start();
	}
}

class FontLoader {
    public static Font pixelFont(float size) {
        try {
            URL url = FontLoader.class.getClassLoader().getResource("PressStart2P-Regular.ttf");
            InputStream fontStream = url.openStream();
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(size);
            fontStream.close();
            return font;
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Arial", Font.PLAIN, (int) size);
        }
    }
}



class GameLabel extends JLabel{
    
	GameLabel(String s, float size){
		super(s);
		setFont(FontLoader.pixelFont(size));
		setForeground(Color.white);
	}
}

class FlickLabel extends JLabel{
	
	FlickLabel(String s){
		super(s);
        setForeground(Color.red);

        Thread flick = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(200);
                    setFont(FontLoader.pixelFont(20));
                    Thread.sleep(200);
                    setFont(FontLoader.pixelFont(0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        flick.start();
	}
}



abstract class GameObject {
	float x,y;
	GameObject(){
		
	}
	abstract void draw(Graphics g);
	void update(float dt) {};
	boolean collisionResolution(GameObject o) {return false;}
}

class Ball extends GameObject{
	float vx, vy;
	float prev_x, prev_y;
	float r;
	float speed;
	double angle;
	
	Ball(int _x, int _y, float _s){
		x = _x;
		y = _y;
		speed = _s;
		
		prev_x = x;
		prev_y = y;
		r = 5;
		
		angle = Math.random()*1.5708 + 0.7853;
		vx = (float)(Math.cos(angle))*speed;
		vy = -(float)(Math.sin(angle))*speed;
	}
	
	Ball(Ball in, float addAngle){
		x = in.x;
		y = in.y;
		speed = in.speed;
		angle = Math.atan2(in.vy, in.vx) + addAngle;
		
		prev_x = x;
		prev_y = y;
		r = 5;
		
		vx = (float) (Math.cos(angle)*speed);
        vy = (float) (Math.sin(angle)*speed);
	}
	@Override
	void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		GradientPaint gradient = new GradientPaint(x, y-r, Color.orange, x, y+r, Color.red);
		g2.setPaint(gradient);
		g2.fillOval((int)(x-r), (int)(y-r), (int)(2*r), (int)(2*r));
		g.setColor(Color.yellow);
		g.fillOval((int)(x-r+2), (int)(y-r+2), (int)(2*r-4), (int)(2*r-4));
	}
	@Override
	void update(float dt) {
		prev_x = x;
		prev_y = y;
		x += vx*dt;
		y += vy*dt;
	}
	@Override
	boolean collisionResolution(GameObject o) {
		if(o instanceof Wall) {
			Wall wall = (Wall) o;
			float left = wall.x - r;
			float right = wall.x + wall.w + r;
			float top = wall.y - r;
			float bottom = wall.y + wall.h + r;
			
			
			if(x>left && x<right && y>top && y<bottom) {
				//color = color.red;
				if(prev_y < top) 	{ y = top - r; 		vy = -vy;	return true;}
				if(prev_y > bottom) { y = bottom + r; 	vy = -vy;	return true;}
				if(prev_x < left) 	{ x = left - r;		vx = -vx;	return true;}
				if(prev_x > right)  { x = right + r; 	vx = -vx;	return true;}	
				
			}
		}
		if(o instanceof Block) {
			Block block = (Block) o;
			
			float left = block.x - r;
			float right = block.x + block.w + r;
			float top = block.y - r;
			float bottom = block.y + block.h + r;
			
			
			if(x>left && x<right && y>top && y<bottom && !block.broken) {
				if(o instanceof YellowBlock) {Audio.playAudio("YellowBlockHit.wav");}
				else {Audio.playAudio("BlockHit.wav");}
				
				//color = color.red;
				block.broken = true;
				if(prev_y < top) 	{ y = top - r; 		vy = -vy;	return true;}
				if(prev_y > bottom) { y = bottom + r; 	vy = -vy;	return true;}
				if(prev_x < left) 	{ x = left - r;		vx = -vx;	return true;}
				if(prev_x > right)  { x = right + r; 	vx = -vx;	return true;}	
			}
		}
		if(o instanceof Paddle) {
			Paddle paddle = (Paddle) o;
			float left = paddle.x - 5 - r;
			float right = paddle.x + paddle.w + 5 + r;
			float top = paddle.y - r;
			float bottom = paddle.y + paddle.h + r;
			
			
			if(x>left && x<right && y>top && y<bottom) {
				Audio.playAudio("PaddleHit.wav");
				//color = color.red;
				angle = ((x-paddle.x)/paddle.w)*1.5708 + 0.7853;
				if(prev_y < top) 	{ y = top - r;}
				if(prev_y > bottom) { y = bottom + r;}
				if(prev_x < left) 	{ x = left - r;}
				if(prev_x > right)  { x = right + r;}	
				vx = -(float)(Math.cos(angle)*speed);
				vy = -(float)(Math.sin(angle)*speed);	
				return true;
			}
		}
		return false;
	}
}



class Paddle extends GameObject{
	float w, h;
	int mode;
	Paddle(int _x, int _y, int _w, int _h){
		x = _x;
		y = _y;
		w = _w;
		h = _h;
		mode = 0;
	}
	@Override
	void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int bright = 0;
		if(mode == 1) {
			bright = 50;
		}
		if(mode == 2) { if(w < 250) {w += 10;} }
		else { if(w > 150) {w -= 10;} }
		
		GradientPaint gradient = new GradientPaint(0, y, new Color(250,250,250), 0 , y+h, new Color(100,100,100));
        g2.setPaint(gradient);
        g2.fillRect((int)x, (int)y, (int)w, (int)h);
		g2.setColor(new Color(150,150,150));
		g2.fillRect((int)x+3, (int)y+3, (int)w-6, (int)h-6);
		
		gradient = new GradientPaint(0, y-2, new Color(255,0,0), 0, y+h+2, new Color(100 + bright,0 + bright,0 + bright));
        g2.setPaint(gradient);
        g2.fillRoundRect((int)(x-2), (int)y-2, 34, 34, 3, 3);
        g2.fillRoundRect((int)(x+w-34), (int)y-2, 34, 34, 3, 3);
		g2.setColor(new Color(200 + bright,0 + bright*2,0 + bright*2));
		g2.fillRoundRect((int)(x+1), (int)(y+1), 28, 28, 3, 3);
		g2.fillRoundRect((int)(x+w-31), (int)(y+1), 28, 28, 3, 3);
	}
	
	void move(int dx) {
		x += dx;
		if(x < 20)
        	x = 20;
		if(x > 765-w)
        	x = 765-w;
	}
	
	void init() {
		mode = 0;
		w = 150;
		x = 320;
	}
}

class Wall extends GameObject{
	float w, h;
	Wall(int _x, int _y, int _w, int _h){
		x = _x;
		y = _y;
		w = _w;
		h = _h;
	}
	@Override
	void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		GradientPaint gradient = new GradientPaint(0, 0, Color.gray, 0, h, Color.darkGray);
        g2.setPaint(gradient);
        g2.fillRect((int)x, (int)y, (int)w, (int)h);
        
		g.setColor(new Color(80,80,80));
		g2.fillRect((int)x+3, (int)y+3, (int)w-6, (int)h-6);
	}
}

class Block extends GameObject{
	float w, h;
	boolean broken;
	int opacity = 255;
	Block(int _x, int _y, int _w, int _h){
		x = _x;
		y = _y;
		w = _w;
		h = _h;
		broken = false;
	}
	@Override
	void draw(Graphics g) {
		if(broken == true) {
			opacity -= 20;
			if(opacity < 0)
				opacity = 0;
		}
		Graphics2D g2 = (Graphics2D) g;
		
		GradientPaint gradient = new GradientPaint(0, y, new Color(250,100,250, opacity), 0, y+h, new Color(100,40,100, opacity));
        g2.setPaint(gradient);
        g2.fillRect((int)x, (int)y, (int)w, (int)h);
        
		g.setColor(new Color(200,80,200, opacity));
		g2.fillRect((int)x+3, (int)y+3, (int)w-6, (int)h-6);
	}
}

class YellowBlock extends Block {
	int bright;
	int time;
	YellowBlock(int _x, int _y, int _w, int _h){
		super(_x,_y,_w,_h);
		bright = 0;
		time = (int)(Math.random()*3000);
		new Thread(()->{
			try {
				Thread.sleep(time);
				
				while(true) {
					bright = 50;
					Thread.sleep(250);
					bright = 0;
					Thread.sleep(2750);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	@Override
	void draw(Graphics g) {
		
		if(broken == true) {
			opacity -= 20;
			if(opacity < 0)
				opacity = 0;
		}
		
		Graphics2D g2 = (Graphics2D) g;
		
		GradientPaint gradient = new GradientPaint(0, y, new Color(250,250,100, opacity), 0, y+h, new Color(100 + bright,100 + bright,40 +bright, opacity));
        g2.setPaint(gradient);
        g2.fillRect((int)x, (int)y, (int)w, (int)h);
        
		g.setColor(new Color(200+ bright,200 + bright,80 + bright, opacity));
		g2.fillRect((int)x+3, (int)y+3, (int)w-6, (int)h-6);
	}
}

class Item extends GameObject{
	float w, h;
	Item(int _x, int _y, int _w, int _h){
		x = _x;
		y = _y;
		w = _w;
		h = _h;
	}
	
	@Override
	void draw(Graphics g) {}
	
	@Override
	void update(float dt) {y += 400*dt;}
	@Override
	boolean collisionResolution(GameObject o) {
		if(o instanceof Paddle) {
			Paddle paddle = (Paddle) o;
			float left = paddle.x - 5 - w/2;
			float right = paddle.x + paddle.w + 5 + w/2;
			float top = paddle.y - h/2;
			float bottom = paddle.y + paddle.h + h/2;
			
			if(x>left && x<right && y>top && y<bottom) {
				return true;
			}
		}
		return false;
	}
}

class MissileItem extends Item{

	MissileItem(int _x, int _y) {
		super(_x, _y, 30, 10);
	}
	
	@Override
	void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		GradientPaint gradient = new GradientPaint(0, y, Color.gray, 0, y+h, Color.darkGray);
        g2.setPaint(gradient);
        g2.fillRoundRect((int)(x), (int)y, (int)w, (int)h, 3, 3);
        g2.setColor(new Color(200,0,0));
        g2.fillRect((int)(x+6), (int)(y), 18, 10);
        
        g2.setFont(FontLoader.pixelFont(10.0f));
        g2.setColor(Color.yellow);
        g2.drawString("M", x+w/2-4, y+h);
	}
}

class Missile extends GameObject{

	float w, h;
	Missile(int _x, int _y){
		x = _x;
		y = _y;
		w = 80;
		h = 60;
	}
	
	@Override
	void draw(Graphics g) {
		g.setColor(Color.white);
		g.fillRoundRect((int)x, (int)y, 20, 60,10,10);
		g.fillRoundRect((int)(x+60), (int)y, 20, 60,10,10);
	}
	
	@Override
	void update(float dt) {
		y -= 800*dt;
	}
	
	@Override
	boolean collisionResolution(GameObject o) {
		if(o instanceof Wall) {
			Wall wall = (Wall) o;
			float left = wall.x;
			float right = wall.x + wall.w;
			float top = wall.y;
			float bottom = wall.y + wall.h;
			
			
			if(x+w/2>left && x-w/2<right && y<bottom) {
				return true;
			}
		}
		if(o instanceof Block) {
			Block block = (Block) o;
			
			float left = block.x;
			float right = block.x + block.w;
			float top = block.y;
			float bottom = block.y + block.h;
			
			if(x+w/2>left && x-w/2<right && y>top && y<bottom && !block.broken) {
				block.broken = true;
				return true;
			}
		}
		return false;
	}
}

class EnlargeItem extends Item{

	EnlargeItem(int _x, int _y) {
		super(_x, _y, 30, 10);
	}
	
	@Override
	void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		GradientPaint gradient = new GradientPaint(0, y, Color.gray, 0, y+h, Color.darkGray);
        g2.setPaint(gradient);
        g2.fillRoundRect((int)(x), (int)y, (int)w, (int)h, 3, 3);
        g2.setColor(new Color(0,0,200));
        g2.fillRect((int)(x+6), (int)(y), 18, 10);
        
        g2.setFont(FontLoader.pixelFont(10.0f));
        g2.setColor(Color.yellow);
        g2.drawString("E", x+w/2-4, y+h);
	}
}

abstract class Scene extends JPanel{
	Scene(){
		
	}
	abstract void init();
}


class TitleScene extends Scene implements KeyListener{
	BlockBreaker frame;
	
 	TitleScene(BlockBreaker frame){
		setLayout(null);
		this.frame = frame;
		
		setFocusable(true);
		addKeyListener(this);
		
		GameLabel l1 = new GameLabel("Java Programming", 40);
		GameLabel s1 = new GameLabel("Java Programming", 40);
		GameLabel l2 = new GameLabel("Homework #5", 40);
		GameLabel s2 = new GameLabel("Homework #5", 40);
		GameLabel l3 = new GameLabel("BlockBreaker", 60);
		GameLabel s3 = new GameLabel("BlockBreaker", 60);
		
		FlickLabel l4 = new FlickLabel("Press Spacebar to play!");
		FlickLabel s4 = new FlickLabel("Press Spacebar to play!");
		
		l1.setBounds(80, 120, 800, 40);
		l2.setBounds(170, 200, 800, 40);
		l3.setBounds(40, 350, 800, 80);
		l4.setBounds(170, 600, 800, 20);
		add(l1);
		add(l2);
		add(l3);
		add(l4);
		
		s1.setBounds(85, 125, 800, 40);
		s1.setForeground(Color.black);
		s2.setBounds(175, 205, 800, 40);
		s2.setForeground(Color.black);
		s3.setBounds(45, 355, 800, 80);
		s3.setForeground(Color.black);
		s4.setBounds(173, 603, 800, 20);
		s4.setForeground(Color.black);
		add(s1);
		add(s2);
		add(s3);
		add(s4);
	}
 	void init() {
 		requestFocus();
 		Audio.playAudio("Game Start.wav");
 	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Background.draw(g);
	}

	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			frame.changeScene(2);
		}
	}
	public void keyReleased(KeyEvent e) {}
}

class GameScene extends Scene implements KeyListener,
										  Runnable{
	BlockBreaker frame;
	LinkedList <GameObject> walls;
	LinkedList <GameObject> balls;
	Item item;
	Paddle paddle;
	Thread gameLoop;
	int level;
	int count;
	float ballspeed;
	
	GameScene(BlockBreaker frame) {
		setLayout(null);
		this.frame = frame;
		level = 0;
		frame.yourScore = 0;
		walls = new LinkedList<>();
		balls = new LinkedList<>();
		
		paddle = new Paddle(320, 700, 150, 30);
		
		gameLoop = new Thread(this);
		
		setFocusable(true);
		addKeyListener(this);
	}
	
    void init() {
        requestFocus();
        Audio.playAudio("Round Start.wav");

        level++;
        ballspeed = 350.0f + 50.0f*level;
        
        balls.clear();
        walls.add(paddle);
		walls.add(new Wall(0,0,790,20));
		walls.add(new Wall(0,20,20,745));
		walls.add(new Wall(767,20,20,745));
        balls.add(new Ball(400,690,ballspeed));
        
        item = null;
        paddle.init();
        
        count = 0;
        int num = level * 3;
        int bw = (751 - (5 * (num + 1))) / num;
        int bh = (400 - (5 * (num + 1))) / num;
        for (int i = 0; i < level * 3; i++) {
            for (int j = 0; j < level * 3; j++) {
            	if((int)(Math.random()*1000)%5 == 0) {walls.add(new YellowBlock(25 + (bw + 5) * i, 25 + (bh + 5) * j, bw, bh));}
            	else {walls.add(new Block(25 + (bw + 5) * i, 25 + (bh + 5) * j, bw, bh));}
            }
        }
        new Thread(() -> {
        	GameLabel l = new GameLabel(String.format("Stage %d", level), 80);
        	l.setBounds(120, 320, 800, 80);
        	try {
        		for(int i = 0; i < 5; i++) {
        			add(l);
        			repaint();
        			Thread.sleep(300);
        			remove(l);
        			repaint();
        			Thread.sleep(300);
        			
        		}
                
                if(level == 1)
                	gameLoop.start();
                Audio.playAudio("PaddleHit.wav");
            }catch (InterruptedException e) {
            	e.printStackTrace();
            }
        }).start();
    }
	
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Background.draw(g);
		
		var it1 = balls.iterator();
		while(it1.hasNext()) 
			it1.next().draw(g);
		
		var it2 = walls.iterator();
		while(it2.hasNext()) 
			it2.next().draw(g);
		if(item != null)
			item.draw(g);
	}
	
    boolean moveLeft = false;
    boolean moveRight = false;
	public void keyTyped(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            moveLeft = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            moveRight = true;
        }
        if (paddle.mode == 1 && e.getKeyCode() == KeyEvent.VK_SPACE) {
        	Audio.playAudio("Missile.wav");
        	balls.add(new Missile((int)(paddle.x+35), (int)(paddle.y - 60)));
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            moveLeft = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            moveRight = false;
        }
    }
	
	public void run() {
		
	    while (true) {
	        try {
	        	//패들 이동 처리
	        	if (moveLeft) {paddle.move(-15);}
                if (moveRight) {paddle.move(15);}
                
                // 아이템이 패들과 닿았을 때 아이템 효과 적용
                if(item != null) {
                	item.update(0.033f);
                	if(item.collisionResolution(paddle)) {
                		if(item instanceof MissileItem) paddle.mode = 1;
                		if(item instanceof EnlargeItem) {
                			Audio.playAudio("Enlarge.wav");
                			paddle.mode = 2;
                		}
                		item = null;
                	}
                	else if(item.y > 800) {
                    	item = null;
                    }
                }
                
                // 충돌 처리 루프
                ArrayList<Ball> newBalls = new ArrayList<>(); // 추가 공을 저장할 임시 배열
	            var it1 = balls.iterator();
	            while (it1.hasNext()) {
	                GameObject o1 = it1.next();
	                o1.update(0.033f);
	                
	                // 범위를 넘어간 공 삭제
	                if(o1.y > 800) {
	                	it1.remove();
	                }

	                var it2 = walls.iterator();
	                while (it2.hasNext()) {
	                    GameObject o2 = it2.next();
	                    
	                    // 충돌 발생시 처리
	                    if (o1.collisionResolution(o2)){
	                    	
	                    	// 미사일이 블록이나 벽에 맞았을 때 처리
	                    	if(o1 instanceof Missile) {
	                    		if(o2 instanceof Wall) {
	                    			it1.remove();
	                    			if(it1.hasNext())
	                    				it1.next();
	                    		}
	                    		
	                    		if(o2 instanceof Block) {
	                    			count++;
	                    			frame.yourScore += 10;
	                    			it1.remove();
	                    			if(it1.hasNext())
	                    				it1.next();
	                    		}
	                    	}
	                    	
	                    	// 볼이 블록에 맞았을 때 처리
	                    	if(o1 instanceof Ball && o2 instanceof Block) {
	                    		count++;
		                        frame.yourScore += 10;
		                    	Block block = (Block)o2;
		                    	
		                    	// 보라색 블럭의 경우 아이템이 필드에 없는 경우만 아이템 생성
		                    	if(item == null && !(o2 instanceof YellowBlock)) {
		                    		if((int)(Math.random()*10000)%5 == 0) {
		                    			item = new MissileItem((int)(block.x + block.w/2 - 15) ,(int)(block.y + block.h/2 - 5));
		                    		}
		                    		else if((int)(Math.random()*10000)%5 == 1) {
		                    			item = new EnlargeItem((int)(block.x + block.w/2 - 15) ,(int)(block.y + block.h/2 - 5));
		                    		}
		                    	}
		                    	
		                    	// 노란 블록 맞았을 시 처리
		                        if(o2 instanceof YellowBlock) {
		                        	 Ball b1 = (Ball)o1;
		                             newBalls.add(new Ball(b1, 0.5236f));
		                             newBalls.add(new Ball(b1, -0.5236f));
		                        }
	                    	}
	                    }
	                }
	            }  
	            // 추가된 공 적용
	            balls.addAll(newBalls);
	            repaint();
	            
	            // 클리어 처리
	            if(count == (level*3)*(level*3)) {
	            	Audio.playAudio("StageClear.wav");
	            	Thread.sleep(2000);
	            	walls.clear();
	            	init();
	            	repaint();
	            	Thread.sleep(3000);
	            }
	            
	            // 공이 없지만 미사일이 남았을 경우 처리
	            var check = balls.iterator();
	            while(check.hasNext()) {
	            	if(check.next() instanceof Ball)
	            		break;
	            	balls.clear();
	            }
	            
	            // 게임 오버 처리
	            if(balls.isEmpty() && level > 0) {
	            	Audio.playAudio("PaddleBroken.wav");
	            	Thread.sleep(2000);
	            	if(frame.yourScore > frame.highScore)
	            		frame.highScore = frame.yourScore;
	            	frame.changeScene(3);
	            	break;
	            }
	            
	            Thread.sleep(33);
	        } catch (Exception e) {
	        	System.out.println(e);
	            return;
	        }
	    }			
	}
}

class GameOverScene extends Scene implements KeyListener{
	BlockBreaker frame;
	
	GameOverScene(BlockBreaker frame){
		setLayout(null);
		this.frame = frame;
		
		setFocusable(true);
		addKeyListener(this);

		GameLabel l1 = new GameLabel("Game Over", 80);
		GameLabel s1 = new GameLabel("Game Over", 80);
		GameLabel l2 = new GameLabel(String.format("Your Score : %d", frame.yourScore), 20);
		GameLabel s2 = new GameLabel(String.format("Your Score : %d", frame.yourScore), 20);
		GameLabel l3 = new GameLabel(String.format("High Score : %d", frame.highScore), 20);
		GameLabel s3 = new GameLabel(String.format("High Score : %d", frame.highScore), 20);
		
		FlickLabel l4 = new FlickLabel("Press Spacebar");
		FlickLabel s4 = new FlickLabel("Press Spacebar");
		
		l1.setBounds(40, 200, 800, 80);
		l2.setBounds(250, 350, 800, 20);
		l3.setBounds(250, 400, 800, 20);
		l4.setBounds(250, 600, 800, 20);
		add(l1);
		add(l2);
		add(l3);
		add(l4);
		

		s1.setBounds(45, 205, 800, 80);
		s1.setForeground(Color.black);
		s2.setBounds(255, 355, 800, 20);
		s2.setForeground(Color.black);
		s3.setBounds(255, 405, 800, 20);
		s3.setForeground(Color.black);
		s4.setBounds(255, 605, 800, 20);
		s4.setForeground(Color.black);
		add(s1);
		add(s2);
		add(s3);
		add(s4);
		
	}
 	void init() {
 		requestFocus();
 		Audio.playAudio("Game Over.wav");
 	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Background.draw(g);
	}

	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			frame.changeScene(1);
		}
	}
	public void keyReleased(KeyEvent e) {}
}


public class BlockBreaker extends JFrame{
	Scene scene;
	int yourScore;
	int highScore;
	
	BlockBreaker() {
		setTitle("Java Homework5 : Block Breaker");
		setSize(800,800);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		new Background();
		new Audio();
		scene = new TitleScene(this);
		
		highScore = 0;
		
		add(scene);
		scene.init();
		
		setVisible(true);
	}
	
	public void changeScene(int sceneNum) {
		remove(scene);
		if(sceneNum == 1) {scene = new TitleScene(this);}
		if(sceneNum == 2) {scene = new GameScene(this);}
		if(sceneNum == 3) {scene = new GameOverScene(this);}
		add(scene);
		scene.init();
		revalidate();
		repaint();
	}
	
	public static void main(String[] args) {
		new BlockBreaker();
	}
}
 