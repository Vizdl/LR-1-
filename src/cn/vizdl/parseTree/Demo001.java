package cn.vizdl.parseTree;

import java.util.LinkedList;

/**
 * Leetcode 224. ����������
 * 2019��7��12��20:43:56
 * @author HP
 *
 */

public class Demo001 {
    /**
	 * tokenԪ�����Ͱ��� + - * / ( ) ��������
	 */
	public enum Type {  
		PLUS, MINUS, MUL, DIV, LPAREN, RPAREN, INTEGER
	} 
	/**
	 * TokenNode��һ��˫������ڵ�
	 */
	class TokenNode{
		public int val;
		public Type type;
		
		public TokenNode(Type type) {
			this.type = type;
		}
		
		public TokenNode(int val, Type type) {
			this.val = val;
			this.type = type;
		}
	}
	class TreeNode{
		public TokenNode val;
		public TreeNode left;
		public TreeNode right;
		
		public TreeNode (TokenNode val) {
			this.val = val;
		}
		
	}
	/**
	 * �����ȼ�����CFG,ͨ�������﷨������,����������ֵ��
	 * �ַ������ʽ���԰��������� ( �������� )���Ӻ� + ������ -���Ǹ������Ϳո�  ��
	 * ��һ�� : �ж��ٸ����ȼ���
	 * ���������ȼ�
	 * �ڶ��� : ���ȼ��ֱ����ʲô
	 * ������ȼ� : ()
	 * ��ͨ���ȼ� : * /
	 * ������ȼ� : + -
	 * ������ : ��дCFG���ʽ
	 * <Goal> ::= <Expr>
	 * <Expr> ::= <Expr> <+> <Term>
	 * 		| <Expr> <-> <Term>
	 * 		| <Expr>
	 * <Term> ::= <Term> <*> <Factor>
	 * 		| <Temp> </> <Factor>
	 * 		| <Temp>
	 * <Factor> ::= <(> <Expr> <)>
	 * 		| <num>
	 * ���Ĳ� : ������ݹ�
	 * 	
	 * ���岽 : ���ַ���ת��Ϊtoken����
	 * ������ : ��token����ת��Ϊ�﷨������
	 * @param s
	 * @return
	 */
	LinkedList<TokenNode> list;
	public int calculate(String s) {
		list = new LinkedList<TokenNode>();
		char[] ch = s.toCharArray();
		this.stringToTokenList(ch);
		TreeNode root = this.tokenListToTree();
		return this.eval(root);
    }
	
	/**
	 * ���ַ���s����Ϣת����list��
	 * @param s
	 */
	private void stringToTokenList (char[] ch) {
		int idx = 0;
		while (idx < ch.length) {
            if (ch[idx] == ' '){
                idx++;
            }else if (ch[idx] == '+') {
				list.add(new TokenNode(Type.PLUS)); idx++;
			}else if (ch[idx] == '-') {
				list.add(new TokenNode(Type.MINUS)); idx++;
			}else if (ch[idx] == '*') {
				list.add(new TokenNode(Type.MUL)); idx++;
			}else if (ch[idx] == '/') {
				list.add(new TokenNode(Type.DIV)); idx++;
			}else if (ch[idx] == '(') {
				list.add(new TokenNode(Type.LPAREN)); idx++;
			}else if (ch[idx] == ')') {
				list.add(new TokenNode(Type.RPAREN)); idx++;
			}else {
				int count = 0;
				while (idx + count < ch.length && ch[idx + count] >= '0' && ch[idx + count] <= '9') {
					count++;
				}
				String s = new String(ch, idx, count);
				idx += count;
				int val = Integer.parseInt(s);
				list.add(new TokenNode(val, Type.INTEGER));
			}
		}
	}
	 //point��list��ָ��
	int point;
	private TreeNode tokenListToTree() {
		point = 0;
		return expr();
	}
	/*
	e.g. 1+2+3+4
	this will first create
	  +
	  /\
	 1  2
	then, add + 3 at the top, 
	     +
		/\
	   +  3
	  /\
	 1  2
	1+2*3 like
	   +
	   /\
      1   *
	      /\
		 2  3
    because plus->right = term(), and term is 2*3
	*/
	/*
	 ֻ���ս����point++;
	 */
	//����expr��˵,�����������������Ӽ���
	private TreeNode expr() {	
		TreeNode left = term();
		
		while (point < list.size() && (list.get(point).type == Type.PLUS || list.get(point).type == Type.MINUS)) {
			TreeNode root = new TreeNode(list.get(point++));
			TreeNode right = term();
			root.left = left;
			root.right = right;
			left = root;
		}return left;
	}
	//����term��˵,�����������������˳�
	//�� * ���� / ����ǰһ�����ſ�ʼ
	private TreeNode term() {	
		TreeNode left = factor();
		
		while (point < list.size() && (list.get(point).type == Type.DIV || list.get(point).type == Type.MUL)) {
			TreeNode root = new TreeNode(list.get(point++));
			TreeNode right = factor();
			root.left = left;
			root.right = right;
			left = root;
		}return left;
	}

	private TreeNode factor() {
		if (list.get(point).type == Type.LPAREN) {
			point++;	//����������
			TreeNode res = expr();
			point++;	//����������
			return res;
		}else {
			return new TreeNode(list.get(point++));
		}
	} 
	
	private int eval(TreeNode root) {
		if (root == null)
			return 0;
		Type type = root.val.type;
		if (type == Type.INTEGER)
			return root.val.val;
		int left = eval(root.left);
		int right = eval(root.right);
		if (type == Type.PLUS) {
			return left + right;
		}else if (type == Type.MINUS) {
			return left - right;
		}else if (type == Type.MUL) {
			return left * right;
		}else{
			return left / right;
		}
	}
}
