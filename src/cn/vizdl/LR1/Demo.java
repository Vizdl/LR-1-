package cn.vizdl.LR1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/*
��Ŀ�� :  LR(1) parser generator (LR(1)�﷨������������)
��Ŀ���� : 
	���� : ����ĳ�ļ��ڴ��ַ�����ڲ�����  <Expr> ::=  <Term> + <Factor>; �Ľṹ�����LR(1)�﷨��
	����Ľ�֧�� BNF��ʽ�ڲ��� �ս��,���ս��,������,;(��ʾ����ʽ����),::=(��ʾΪ����Ϊ)
	�����ﲻ֧�ֱհ�,Ҳ����{},��Ϊ�հ�����ת��Ϊ���ս���ĵݹ顣
		�����ı���ʽ :
			start : <aim_name>  //aim_name��ʾ��ʼ�������� 
		���� : 
			start : <Goal>;
			<Goal> ::= <Expr>;
			<Expr> ::= <Expr> "+" <Term> | <Expr> "-" <Term>;
			<Term> ::= <Term> "*" <Factor> | <Term> "/" <Factor>;
			<Factor> ::= "number";
			#
		��#��Ϊ��β
		������� : ��Ϊ�������޹��﷨��һ����Ԫ��,��LR(1)�﷨�����������޹��﷨���Ӽ������Բ�����Ԫ�����ʽ����ʾLR(1)�﷨,�ǲ�����ʧ��Ϣ�ġ�
			��Ԫ�� (T,NT,S,P)
			T �� �ս������
			NT : ���ս������
			S : �﷨����ʼ����(���ս��)
			P : ����ʽ���� 
			T, NT��������һ��hash_set����ʾ��
			P ���Է�Ϊ��������,���һ����һ�����ս��,�Ҳ���һ��֧�ֻ�����Ĳ���ʽ�� 
			����ʽ��˿���ʹ��Node�ڵ�����ʾ,����ʽ�Ҷ˿���ʹ�ö������(�����м���ȡ���ڵ�ǰ����ʽ�ж��ٸ��������)����ʾ��
			�������﷨��Ϊ����,��һ����Expr,�ڶ�������Term,������������Factor
			<Expr> ::= <Term> { "|" <Term>}; //����ʽ(���ʽ)���Ա��ɶ��С���� �� ����
			<Term> ::= <Factor> { "+" <Factor>}; // + ��ʾ����
			<Factor> ::= <T> | <NT>
	��� ��Action �� GoTo �� 
*/ 
public class Demo {
	public static void main (String[] args) {
		//������Ĳ���ʽ������ch��
		Scanner scanner = new Scanner(System.in);
		String s = new String();
		String c;
		//���봦��...
		while (true) {
			c = scanner.nextLine();
			int i;
			for (i = 0; i < c.length(); i++) {
				if (c.charAt(i) != '#')
					s += c.charAt(i);
				else {
					scanner.close();
					break;
				}
			}
			if (i != c.length()) {
				break;
			}
		}
		//
		BnfContainer bc = new BnfContainer();
		CodeAnalyzer ca = new CodeAnalyzer(s, bc);
		ca.analyze();
		bc.printBNF();
		
	}
}

/**
 * ����װ��BNF��ʽ����Ϣ��
 */
class BnfContainer {
	/**
	 * �ڲ���,NT�Ľڵ㡣
	 * @author HP
	 */
	class NTNode {
		private String name; //����id
		private List<List<Integer>> expr;
		public NTNode(String name) {
			expr = new ArrayList<List<Integer>>();
			this.name = name;
		}
		/**
		 * ���һ��expr
		 * �������expr���±�
		 * @return
		 */
		public int addExpr() {
			expr.add(new ArrayList<Integer>());
			return expr.size() - 1;
		}
		/**
		 * ���±�Ϊidx��expr���value
		 * @param idx
		 * @param value
		 */
		public void addExprElement (int idx, int value) {
			this.expr.get(idx).add(value);
		}
		/**
		 * �����һ�����ʽ���value
		 * @param value
		 */
		public void addExprElement (int value) {
			this.addExprElement(this.expr.size() - 1, value);
		}
		
		public void printNTNode () {
			System.out.println("NTNumber : " + this.name);
			for (List<Integer> list : this.expr) {
				for (Integer val : list) {
					System.out.print(val + " ");
				}System.out.println();
			}
		}
	}
	
	
	//��������
	/**
	 * ����������ֻ�������ս��
	 * ��ΪҪ���ս���ͷ��ս��
	 * ����ͬһ��������
	 * ����ʹ�����������ս���ͷ��ս����
	 */
	private static final int MASK = 0X80000000; //����,�������ս�������εı��롣
	private static final int DECODE = 0X7fffffff; //����,��������õ�ԭ���ı��롣
	/**
	 * ���ս��Map 
	 * key : ���ս������
	 * value : ���ս����production�����е��±�
	 */
	private HashMap<String,Integer> NTMap;
	/**
	 * �ս��Map 
	 * key : �ս������
	 * value : �ս����T�����е��±�
	 */
	private HashMap<String,Integer> TMap;
	// �ս������
	private ArrayList<String> T;
	// ����ʽ����,��Ϊһ�����ս��һ������ʽ����˫���ϵ��
	private ArrayList<NTNode> production;
	//����δ����,Ĭ��Ϊ0
	public int startIndex = 0;
	public BnfContainer() {
		//�ڲ����ݽṹ��ʼ��
		NTMap = new HashMap<String,Integer>();
		TMap = new HashMap<String,Integer>();
		T = new ArrayList<String>();
		production = new ArrayList<NTNode>();
	}
	
	/**
	 * ���ÿ�ʼ���ս��
	 * @param name
	 */
	public void setStart (String name) {
		this.addNT(name);
		this.startIndex = this.NTMap.get(name);
	}
	
	/**
	 * �����ս�������ִ���,�������һ�����ս���ڵ㡣
	 * @param name
	 */
	public void addNT (String name) {
		if (name.isEmpty()) {
			System.out.println("�ս������Ϊ��");
			System.exit(-1);
		}
		if (!NTMap.containsKey(name)) {
			NTNode node = new NTNode(name);
			NTMap.put(name, production.size());
			production.add(node);
		}
	}
	
	/**
	 * ���ս������,���ӷ��ս����
	 * @param name
	 */
	public void addT(String name) {
		if (!this.TMap.containsKey(name)) {
			this.TMap.put(name, T.size());
//System.out.println(name);
			this.T.add(name);
		}
	}
	
	/**
	 * �����ս������
	 * ��ȡ�ս�����
	 * �������ڵ�ǰ�ս��,���ر��
	 * ���򷵻�-1,������󾯸沢���˳���
	 * @param name
	 * @return
	 */
	private int getTSerialNumber (String name) {
		this.notFindTWarning(name);
		return this.TMap.get(name) | BnfContainer.MASK;
	}
	
	/**
	 * ������ս������
	 * ��ȡ���ս�����
	 * �������ڵ�ǰ���ս��,���ر��
	 * ���򷵻�-1,������󾯸沢���˳���
	 * @param name
	 * @return
	 */
	private int getNTSerialNumber (String name) {
		this.notFindNTWarning(name);
		return this.NTMap.get(name);
	}
	
	/**
	 * �����µı��ʽ����ӵ�����Ϊname�ķ��ս���ڵ���
	 * ���ر��ʽ���
	 */
	public int creatNewExper(String name) {
		this.notFindNTWarning(name);
		NTNode ntn = this.production.get(this.NTMap.get(name));
		return ntn.addExpr();
	}
	/**
	 * ����˷��ս������Ϊname�Ĳ���ʽ
	 * ��idx���ʽ���Ԫ��
	 * @param name
	 * @param idx
	 * @param isNt
	 */
	public void addExpeElement(String name, int idx,boolean isNt, String addElement) {
		NTNode ntn = this.production.get(this.NTMap.get(name));
		if (isNt) {
			this.notFindNTWarning(name);
			this.notFindNTWarning(addElement);
			ntn.addExprElement(idx, this.getNTSerialNumber(addElement));
		}else {
			this.addT(addElement);
			ntn.addExprElement(idx, this.getTSerialNumber(addElement));
		}
	}
	
	/**
	 * ����˷��ս������Ϊname�Ĳ���ʽ
	 * ���һ�����ʽ���Ԫ��
	 * @param name
	 * @param list
	 */
	public void addExpeElement(String name,boolean isNt, String addElement) {
		NTNode ntn = this.production.get(this.NTMap.get(name));
		if (isNt) {
			this.notFindNTWarning(name);
			this.notFindNTWarning(addElement);
			ntn.addExprElement(this.getNTSerialNumber(addElement));
		}else {
			this.addT(addElement);
			ntn.addExprElement(this.getTSerialNumber(addElement));
		}
	}
	
	/**
	 * �����ҵ��˵�ǰ���ս��,ʲô�����ᷢ����
	 * �������ʾ�����˳�����
	 * @param name
	 */
	private void notFindNTWarning(String name) {
		if (!this.NTMap.containsKey(name)) {
			System.out.println("����ķ��ս��" + name + "!");
			System.exit(-1);
		}
	}
	/**
	 * �����ҵ��˵�ǰ�ս��,ʲô�����ᷢ����
	 * �������ʾ�����˳�����
	 * @param name
	 */
	private void notFindTWarning(String name) {
		if (!this.TMap.containsKey(name)) {
			System.out.println("������ս��" + name + "!");
			System.exit(-1);
		}
	}

	public void printBNF() {
		System.out.println("��ʼ���ս��Ϊ : " + this.production.get(startIndex).name);
		System.out.println("�ս����Ӧ�� : ");
		for (int i = 0; i < this.T.size(); i++) {
			System.out.println(this.T.get(i) + " : " + (i | MASK));
		}
		System.out.println("���ս����Ӧ�� : ");
		for (int i = 0; i < this.production.size(); i++) {
			System.out.println(this.production.get(i).name + " : " + i);
		}
		for (NTNode ntn : this.production) {
			ntn.printNTNode();
		}
	}
}

/**
 * ���������
 * ���Խ�����ת��Ϊ��Ϣ�ȼ۵����ݽṹ
 */
class CodeAnalyzer {
	class Token{
		boolean isNt;
		String name;
		public Token (boolean isNt, String name) {
			this.isNt = isNt;
			this.name = name;
		}
	}
	private char[] text;
	private int textSize = 0; //�ַ�����Ч����
	private int point = 0; //text�������ȵ�ָ��
	private BnfContainer bc;
	private Token token;
	String left; //�����ս��
	private int count = 0; //��¼��ǰ�Ѿ��������ĸ�����ʽ��
	public CodeAnalyzer (String text, BnfContainer bc) {
		this.bc = bc;
		//��ʼ�����������
		this.initText(text);
		this.initStartSymbol();
		this.initCodeAnalyzer();
	}
	/**
	 * �����ַ����ı�,���ش�����ϵ��ַ����顣
	 * @param s
	 * @return
	 */
	private void initText(String s) {
		this.text = s.toCharArray();
		int idx = 0;
		//���ַ�����Ϊһ�����յ��ַ�����(ȥ��һЩ�������ַ�)
		while (idx < text.length) {
			if (text[idx] == '\r' || text[idx] == '\n' || text[idx] == '\t' || text[idx] == ' ') {
				idx++;
			}else {
				text[textSize++] = text[idx++];
			}
		}
	}

	private void initStartSymbol() {
		// ��֤�Ƿ����start:<
		point = 0;
		char[] needle = { 's', 't', 'a', 'r', 't', ':', '<' };
		if (textSize <= needle.length) {
			this.notFindStartNT();
		}
		point = 0;
		while (point < needle.length) {
			if (needle[point] == text[point]) {
				point++;
			} else {
				this.notFindStartNT();
			}
		}
		point = needle.length;
		while (point < textSize && text[point] != '>') {
			point++;
		}
		this.bc.setStart(new String(text, needle.length, point - needle.length));
		this.skip(Type.RT);
		this.skip(Type.SEMICOLON);
	}
	/**
	 * ͨ��skip�������ַ�
	 */
	enum Type{
		LT, //�������
		RT, //�Ҽ�����
		SEMICOLON, //�ֺ�
		QUOTE, //˫����
		OR, //��
		COLON, // :
		EQ, //���ں�
	}
	private void skip (Type t) {
		switch(t) {
		case LT:
			this.skip('<');
			break;
		case RT:
			this.skip('>');
			break;
		case OR:
			this.skip('|');
			break;
		case SEMICOLON:
			this.skip(';');
			break;
		case QUOTE:
			this.skip('"');
			break;
		case COLON:
			this.skip(':');
			break;
		case EQ:
			this.skip('=');
			break;
		}
	}
	private void skip (char c) {
		if (point >= this.textSize || this.text[point] != c) {
			System.out.println("��" + this.count + "������ʽ,ȱ�ٷ���  " + c);
			System.exit(-1);
		}
		point++;
	}
	/**
	 * ���� : û���ҵ�Ŀ��(��ʼ)���ս����! ���˳�����
	 */
	private void notFindStartNT() {
		System.out.println("û���ҵ�Ŀ����ս����!");
		System.exit(-1);
	}

	/**
	 * ֮����һ��ʼ��Ҫ��ӷ��ս��,�����ڽ���BNFʱ�����
	 * ����Ϊ,���ս�����ڶ��������,���� û�ж���
	 * ����ʹ��(ֻ���Ҳ����,δ����ඨ��),������Ǵ���ġ�
	 */
	private void initCodeAnalyzer() {
		int idx = this.point;
		this.point = 0;
		this.count = 0;
		while (true) {
			while (this.point < textSize && text[this.point] != ';') {
				this.point++;
			}this.point++;
			this.count++;
			//�����ֺź���û��������
			if (this.point >= textSize) {
				break;
			}
			String name = this.getNT();
			bc.addNT(name);
		}this.count = 0;
		this.point = idx;
	}

	/**
	 * BNF
	 * ��point��ʼ�����ַ�����
	 * <Goal> ::= {<Production>}
	 * <Production> ::= <�����ս��> "::=" <Expr>;
	 * <Expr> ::= <Term> { "|" <Term>}";";
	 * <Term> ::= {<Factor>}; 	//Term������Ƕ���ս������ս��������
	 * <Factor> ::= <T> | <NT>
	 */
	public void analyze() {
		while (point < this.textSize) {
			this.count++;
			production();
		}
	}
	
	public void production(){
		//�����������ս��
		this.left = this.getNT();
		this.skipDefineSymol();
		this.expr();
	}
	/**
	 * ���� ::=
	 */
	public void skipDefineSymol() {
		skip(Type.COLON);
		skip(Type.COLON);
		skip(Type.EQ);
	}
	/**
	 * ��ȡ���ս��
	 * <xxx>
	 */
	public String getNT () {
		skip(Type.LT);
		StringBuilder res = new StringBuilder();
		while (this.point < this.textSize && text[this.point] != '>') {
			res.append(text[this.point++]);
		}
		skip(Type.RT);
		return res.toString();
	}
	
	/**
	 * ��ǰָ��ָ�� "T" �е�һ��"
	 * @return
	 */
	public String getT() {
		this.skip(Type.QUOTE);
		StringBuilder res = new StringBuilder();
		while (this.point < this.textSize && this.text[this.point] != '"') {
			res.append(text[this.point++]);
		}
		this.skip(Type.QUOTE);
		return res.toString();
	}
	
	/**
	 * ��ǰָ��ָ�� ::= <T>... �� = ��һ������
	 */
	public void expr(){
		this.term();
		while (this.point < this.textSize && text[this.point] == '|') {
			this.skip(Type.OR);
			term();
		}this.skip(Type.SEMICOLON);
	}
	
	/**
	 * �������з���,��ǰ����ָ�� �ս������ս���ķ���  < ���� "
	 */
	public void term(){
		//����һ�����ڵ�ǰterm������
		bc.creatNewExper(this.left);
		while (this.point < this.textSize && (text[this.point] == '"' || text[this.point] == '<')) {
			factor();
			bc.addExpeElement(this.left, token.isNt, token.name);
		}
	}
	
	/**
	 * ͨ��factor��ȡtoken
	 */
	public void factor(){
		//���ս��
		if (text[this.point] == '"') {
			String name = this.getT(); 
			this.token = new Token(false, name);
		}else {
			String name = this.getNT();
			token = new Token (true, name);
		}
	}
}
