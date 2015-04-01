package org.cloudcoder.snapshotanalzyer;

import java.util.Stack;

public class TreePrinter {
	private static class Item {
		private Node node;
		private int remainingChildren;
		private boolean visited;
		
		public Item(Node node) {
			this.node = node;
			this.remainingChildren = node.getChildren().size();
			this.visited = false;
		}
		
		public boolean isVisited() {
			return visited;
		}
		
		public void setVisited(boolean visited) {
			this.visited = visited;
		}
		
		public Node getNode() {
			return node;
		}
		
		public int getRemainingChildren() {
			return remainingChildren;
		}

		public Node nextChild() {
			if (remainingChildren <= 0) {
				throw new IllegalStateException();
			}
			Node child = node.getChildren().get(node.getChildren().size() - remainingChildren);
			--remainingChildren;
			return child;
		}
	}
	
	private Stack<Item> stack;
	
	public TreePrinter() {
		stack = new Stack<Item>();
	}
	
	public void print(Node node) {
		stack.push(new Item(node));
		
		while (!stack.isEmpty()) {
			Item item = stack.pop();
			if (!item.isVisited()) {
				printNode(item.getNode());
				item.setVisited(true);
			}
			if (item.getRemainingChildren() > 0) {
				stack.push(item);
				stack.push(new Item(item.nextChild()));
			}
		}
	}

	private void printNode(Node node) {
		for (int i = 0; i < stack.size(); i++) {
			boolean parentLevel = (i == stack.size() - 1);
			if (parentLevel) {
				System.out.print("+--");
			} else {
				System.out.print(stack.get(i).getRemainingChildren() > 0 ? "|  " : "   ");
			}
		}
		
		System.out.println(node.toString());
	}
}
