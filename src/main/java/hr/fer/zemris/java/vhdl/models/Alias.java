package hr.fer.zemris.java.vhdl.models;

/**
 * Created by Dominik on 28.8.2016..
 */
public class Alias {
	private String original;
	private Integer position;

	public Alias(String original, Integer position) {
		this.original = original;
		this.position = position;
	}


	public String getOriginal() {
		return original;
	}

	public Integer getPosition() {
		return position;
	}
}
