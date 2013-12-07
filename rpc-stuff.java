/***
 * Excerpted from "Code in the Cloud",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/mcappe for more book information.
***/

public interface Fact extends Remote {
    int fact(int n);
}

int j = f.fact(n);

System.out.println("Foo = " + Math.log(3 * f.fact(n)));

f.fact(n, new AsynchCallback<int>() {
        public void onSuccess(int result) {
            System.out.println("Foo = " + Math.log(3 * result));
        }
    });
