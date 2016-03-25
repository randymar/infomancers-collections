# Writing Iterators Today #

When writing iterators which do not just return a wrapped collection, there are two options:
  1. Create a collection in memory, add all relevant items to it in the order required, and return the list.
  1. Save the state of the iterator (current index in array, current iterator location in a collection, stack in a tree, etc) and every time the next() calculate the next element on the spot.

### Why not use the first option ###

The first option is the easy option: Write the code like you would normally, add all elements to a list, and return the list's iterator.

However, this means that a list needs to be created in memory, which is an additional data structure which takes space and time to create - The first item will be returned only after all the list is created.

Worse still: If nested iterators are needed (for example, an iterator to iterate all elements in a graph and another to iterate through all elements corresponding to a certain filter) the time is doubled: First, to calculate the list of all elements in the graph, and then iterating through the result list to create a second list of all elements matching the filter query.

### What is the problem with keeping state ###

Keeping state is as simple or hard as the task at hand. For example, keeping the state of an iteration over an array is keeping the index of the current element as a member of the iterator:

```
public ArrayIterator implements Iterator {
  private int idx;
  private Object[] arr;
  
  public boolean hasNext() { return idx == arr.length; }
  public Object next() { return arr[idx++]; }
}
```

However, keeping the state of iteration over TreeModel nodes might be harder:

```
public final class TreeModelIterator implements Iterator
 {
  private final TreeModel model;
  private final Stack childLocations = new Stack();
  private final Position.Bias bias;

  private TreePath current;

  public TreeModelIterator(TreeModel model, Position.Bias bias) {
    this.model = model;
    this.bias = bias;

    Object root = model.getRoot();
    current = root != null ? new TreePath(root) : null;
  }

  public boolean hasNext() {
    return current != null;
  }

  public TreePath next() {
    TreePath result = current;
    current = null;

    if (model.getChildCount(result.getLastPathComponent()) > 0) {
      pushElementChildren(result);
      Object nextChild = model.getChild(result.getLastPathComponent(),
                                       childLocations.peek());
      current = result.pathByAddingChild(nextChild);
    } else {
      TreePath parent = result.getParentPath();
      while (current == null && childLocations.size() > 0) {
        int nextLocation = childLocations.pop() + getIncrement();
        if (nextLocation == getChildIterationStopper(parent)) {
          parent = parent.getParentPath();
        } else {
          Object nextChild = model.getChild(parent.getLastPathComponent(),
                                                 nextLocation);
          current = parent.pathByAddingChild(nextChild);
          childLocations.push(nextLocation);
        }
      }
    }

    return result;
  }

  private int getChildIterationStopper(TreePath path) {
    return bias == Position.Bias.Forward
      ? model.getChildCount(path.getLastPathComponent()) : -1;
  }

  private void pushElementChildren(TreePath path) {
    int iterationStart = bias == Position.Bias.Forward
      ? 0 : model.getChildCount(path.getLastPathComponent()) - 1;

    childLocations.push(iterationStart);
  }

  private int getIncrement() {
    return bias == Position.Bias.Forward ? 1 : -1;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
```

# Yielding Results #

Yielding a result means using the second option, only letting the compiler or run-time do all the work for you. The result is that a developer writes a method returning an Iterable interface, and in the method the implementation assumes that **the state of all local variables** (explicit and implicit) **is saved automatically**, and that **subsequent calls to the next() method** of the iterator will start not from the beginning of the method, but **will start instead from the last yielding point**.

For example, using the psudo-keyword _yield_, the previous code could be rewritten to be:

```
public final class TreeModelYielder extends Yielder
    implements Iterable {
  private TreeModel model;
  private TreePath path;
  private Position.Bias bias;

  public TreeModelYielder(TreeModel model, Position.Bias bias) {
    this(model, model.getRoot() != null ? new TreePath(model.getRoot()) : null, bias);

  private TreeModelYielder(TreeModel model, TreePath path, Position.Bias bias) {
    this.curPath = path;
    this.bias = bias;
  }

  public void iterator() {
    for (int i = getFirstChildIndex();
        i != getChildIterationStopper(path); 
        i += getIncrement()) {
      Object curChild = model.getChild(path.getLastPathComponent(), i);
      for (TreePath res : new TreeModelYielder(model, curChild, bias)) {
        yield res;
      }
    }

    yield path.getLastPathComponent();
  }

  private int getFirstChildIndex() {
    return bias == Position.Bias.Forward
      ? 0 : model.getChildCount(path.getLastPathComponent()) - 1;
  }

  private int getChildIterationStopper(TreePath path) {
    return bias == Position.Bias.Forward
      ? model.getChildCount(path.getLastPathComponent()) : -1;
  }

  private int getIncrement() {
    return bias == Position.Bias.Forward ? 1 : -1;
  }
}
```

The stack of previous locations is created implicitly and is not maintained manually, and as you can see, the code of iteration is even recursive. The yielding feature takes care of keeping the state of the recursion as well.

Since there was no intent on changing the Java language, no real _yield_ keyword was added. Instead, we've created a small library allowing the feature to exist using bytecode manipulation and class load instrumentation. See how in the [Examples](Examples.md) page.