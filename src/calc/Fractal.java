package calc;

public class Fractal {
	
	private String name;
	
	public Fractal(String name)
	{
		this.name = name;
	}
	
	public Complex ZIterative(Complex Z, Complex C)
	{
		return new Complex(0, 0);
	}
	
	public String getName()
	{
		return name;
	}
}
