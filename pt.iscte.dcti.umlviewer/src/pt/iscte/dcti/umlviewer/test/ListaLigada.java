package pt.iscte.dcti.umlviewer.test;

public class ListaLigada implements Lista, Iteravel {

	public Iterador iterador() {
		return new ListaIterador();
	}

	public void adicionar(Object obj) {
	}

	public void devolver(int index) {
	}

	public void remover(int index) {
	}

	public int count() {
		return 0;
	}
	
	

}
