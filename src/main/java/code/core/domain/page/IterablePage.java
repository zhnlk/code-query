package code.core.domain.page;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

public class IterablePage<E> implements Iterator<E>, Iterable<E> {

    private final PageRequest pageRequest;
    private final Function<PageRequest, Slice<E>> findFunction;
    private final Consumer<PageRequest> perPageHook;

    protected int pageCount;
    protected boolean lastPage;
    protected Iterator<E> perPageIterator;


    public IterablePage(PageRequest pageRequest,
                        Function<PageRequest, Slice<E>> findFunction,
                        Consumer<PageRequest> perPageHook) {

        this.findFunction = findFunction;
        this.pageRequest = pageRequest;
        this.perPageHook = perPageHook;
        this.pageCount = -1;
        this.lastPage = false;

    }


    public IterablePage(PageRequest pageRequest,
                        Function<PageRequest, Slice<E>> findFunction) {
        this(pageRequest, findFunction, PageRequest -> {});
    }


    @Override
    public boolean hasNext() {

        if (pageCount == -1) {
            // initial read.
            nextPage();
        }

        if (!perPageIterator.hasNext() && !lastPage) {
            // Supply from page
            nextPage();
        }

        return !lastPage || perPageIterator.hasNext();
    }


    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return perPageIterator.next();
    }


    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported.");
    }


    @Override
    public Iterator<E> iterator() {
        return this;
    }


    protected void nextPage() {
        pageCount++;
        PageRequest pr = pageRequest.withNumber(pageCount);
        perPageHook.accept(pr);
        Slice<E> slice = findFunction.apply(pr);
        lastPage = !slice.hasNext();
        perPageIterator = slice.getContent().iterator();
    }

}
