package  com.example.hackathon.sorteddata.avltree;

import java.util.Comparator;

class AVLNodeFilled<T> extends AVLNode<T> {

	final AVLNode<T> left, right;
	final T value;
	private final int height, balance, size;
	public AVLNodeFilled(Comparator<T> comparator, T value, AVLNode<T> left, AVLNode<T> right) {
		super(comparator);
		this.value = value;
		this.left = left;
		this.right = right;
		this.size = left.size() + right.size() + 1;
		this.height = Math.max(left.height(), right.height())+1;

		// TODO: Overwrite the following line to correctly compute the tree's balance factor
		this.balance = left.height() - right.height();
	}

	public int height() {
		return height;
	}
	public int balanceFactor() {
		return balance;
	}
	public int size() {
		return size;
	}

	public String toString() {
		if (left instanceof AVLNodeEmpty<T> && right instanceof AVLNodeEmpty<T>)
			return value.toString();
		else
			return "%s -> (%s, %s)".formatted(value.toString(), left.toString(), right.toString());
	}

	private AVLNodeFilled<T> balanceAVL(AVLNodeFilled<T> node) {
		if (node.balance > 1) { // left-heavy
			if (node.left.balanceFactor() < 0){
				// LR
				return new AVLNodeFilled<>(comparator, node.value,
						((AVLNodeFilled<T>) node.left).leftRotate(), node.right).rightRotate();
			}

			return node.rightRotate(); // LL
		}
		if (node.balance < -1) { // right-heavy
			if (node.right.balanceFactor() > 0){
				// RL
				return new AVLNodeFilled<>(comparator, node.value,
						node.left, ((AVLNodeFilled<T>) node.right).rightRotate()).leftRotate();
			}

			return node.leftRotate(); // RR
		}
		return node;
	}


	public AVLNodeFilled<T> insert(T element) {
		// TODO: Complete this method
		int heightDiff = comparator.compare(element, value);

		if (heightDiff < 0) {
			// Insert into left subtree
			AVLNode<T> nodeToLeft = left.insert(element);
			AVLNodeFilled<T> newNode = new AVLNodeFilled<>(comparator, value, nodeToLeft, right);
			return balanceAVL(newNode);
		} else if (heightDiff > 0) {
			// Insert into right subtree
			AVLNode<T> nodeToRight = right.insert(element);
			AVLNodeFilled<T> newNode = new AVLNodeFilled<>(comparator, value, left, nodeToRight);
			return balanceAVL(newNode);
		}
		return this;
	}

	/**
	 * Executes a left rotation on the current node, as defined
	 * by the AVL Tree algorithm.
	 * @return the new node taking this node's place after rotation
	 */
	private AVLNodeFilled<T> leftRotate() {
		// TODO: Complete this method
		AVLNodeFilled<T> rightChild = (AVLNodeFilled<T>) right;
		AVLNode<T> newLeft = new AVLNodeFilled<>(comparator, value, left, rightChild.left);
		return new AVLNodeFilled<>(comparator, rightChild.value, newLeft, rightChild.right);
	}

	/**
	 * Executes a right rotation on the current node, as defined
	 * by the AVL Tree algorithm.
	 * @return the new node taking this node's place after rotation
	 */
	private AVLNodeFilled<T> rightRotate() {
		// TODO: Complete this method
		AVLNodeFilled<T> leftChild = (AVLNodeFilled<T>) left;
		AVLNode<T> newRight = new AVLNodeFilled<>(comparator, value, leftChild.right, right);
		return new AVLNodeFilled<>(comparator, leftChild.value, leftChild.left, newRight);
	}

	public T getAtIndex(int i) {
		if (i < left.size()) return left.getAtIndex(i);
		else if (i == left.size()) return value;
		return right.getAtIndex(i - left.size() - 1);
	}

	public boolean contains(T element) {
		if (comparator.compare(value, element) < 0) {
			return right.contains(element);
		} else if (comparator.compare(element, value) < 0) {
			return left.contains(element);
		}
		return true;
	}

	public T get(T element) {
		if (comparator.compare(value, element) < 0) {
			return right.get(element);
		} else if (comparator.compare(element, value) < 0) {
			return left.get(element);
		}
		return value;
	}
}
