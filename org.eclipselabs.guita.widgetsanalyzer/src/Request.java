import org.eclipse.swt.graphics.RGB;


public class Request {

	private String location;
	private String color;
	private RGB colorRGB;

	public Request(String location, String color){
		this.location = location;
		this.color = color;

		if(color != null)
			setColorRGB(color);
	}

	public String getLocation(){
		return location;
	}

	public String getColor(){
		return color;
	}

	public RGB getColorRGB(){
		return colorRGB;
	}

	public void setColorRGB(String color){
		if(color.equals("Red")){
			colorRGB = new RGB(255, 0, 0);
		}else if(color.equals("Blue")){
			colorRGB = new RGB(0, 0, 255);
		}else if(color.equals("Green")){
			colorRGB = new RGB(0, 255, 0);
		}else if(color.equals("Yellow")){
			colorRGB = new RGB(255, 255, 0);
		}else{
			colorRGB = new RGB(255, 0, 255);
		}
	}
}
