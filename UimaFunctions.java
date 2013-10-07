package edu.mayo.bmi.uima.core.tools;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/*
 * The UimaFunctions is a helper class that performs many of the common functions needed by Annotation Engines.
 */
public class UimaFunctions
{

	/*
	 * Returns the path of the file being processed
	 */
	public static String getDocumentPath(JCas cas)
	{
		String path = "" ;

		FSIterator<?> it = cas.getAnnotationIndex( SourceDocumentInformation.type ).iterator() ;
		if ( it.hasNext() )
		{
			SourceDocumentInformation fileLoc = (SourceDocumentInformation) it.next() ;
			try
			{
				path = new URL( fileLoc.getUri() ).getPath() ;
			}
			catch (Exception e)
			{
				e.printStackTrace() ;
			}
		}
		else
		{
			try
			{
				Scanner s = new Scanner(new File("fname.txt")) ;
				return s.nextLine() ;
			}
			catch (Exception e)
			{
				e.printStackTrace() ;
			}
		}

		return path ;
	}
	
	public static String getFileName(JCas cas)
	{
		String path = getDocumentPath(cas) ;
		
		return path.substring(path.lastIndexOf("/")+1) ;
	}

	/**
	 * Given an FSIndex, iterate through it and return the annotations as a collection.
	 * @param index the FSIndex to iterate through
	 * @return the annotations in FSIndex as a collection. 
	 */
	public static Collection<Annotation> copyFSIteratorToCollection(FSIndex<? extends Annotation> index) {
		Collection<Annotation> annotations = new ArrayList<Annotation>() ;
		FSIterator<? extends Annotation> iterator = index.iterator() ;
		while ( iterator.hasNext() )
		{
			annotations.add(iterator.next() ) ;
		}
		return annotations ;
	}
	
	/*
	 * Returns a collection of annotations of the specified type.
	 */
	public static Collection<Annotation> getAllAnnotataionsOfType(JCas cas, Type type )
	{
		return copyFSIteratorToCollection(cas.getAnnotationIndex( type ));
	}


	
	/*
	 * Returns a collection of annotations of the int type. This parameter should be <name of the annotation class>.type like Markable.type
	 */
	public static Collection<Annotation> getAllAnnotataionsOfType(JCas cas, int type )
	{
		return copyFSIteratorToCollection(cas.getAnnotationIndex( type ));
	}

	/*
	 * Returns the text in between annotations. First must be a non-overlapping and previous to second.
	 */
	public static String getTextInbetween(JCas cas, Annotation first, Annotation second )
	{
		// null
		if ( first == null || second == null )
			return "" ;

		// in the wrong order or overlapping
		if ( first.getEnd() >= second.getBegin() )
			return "" ;

		// recursive
		if ( first.getBegin() == second.getBegin() )
			return "" ;

		return cas.getDocumentText().substring( first.getEnd(), second.getBegin() ) ;
	}

	/*
	 * Returns true if the first annotation completely spans the second. False otherwise.
	 */
	public static boolean completelyCovers( Annotation first, Annotation second )
	{
		if ( first.getBegin() <= second.getBegin() && first.getEnd() >= second.getEnd() )
			return true ;
		return false ;
	}


	/*
	 * Gets all annotations that include the annotation in its span.
	 */
	public static <T extends Annotation> Collection<T> getAllContainingAnnotations(JCas cas, T annotation )
	{
		Collection<T> annotations = new ArrayList<T>() ;
		T current = null ;
		FSIndex<?> index = cas.getAnnotationIndex() ;
		FSIterator<?> iterator = index.iterator() ;
		while ( iterator.hasNext() )
		{
			current =  (T) iterator.next() ;
			if ( completelyCovers( current, annotation ) )
				annotations.add( current ) ;
		}
		return annotations ;
	}

	
	/*
	 * Gets all annotations of int type that include the annotation in its span.
	 */
	public static Collection<Annotation> getAllContainingAnnotationsOfType(JCas cas, Annotation annotation, Type type )
	{
		FSIndex<?> index = cas.getAnnotationIndex( type ) ;
		return getAllContainingAnnotationsOfTypeBaseMethod(annotation, index);
	}
	
	/*
	 * Gets all annotations of type that include the annotation in its span.
	 */
	public static Collection<Annotation> getAllContainingAnnotationsOfType(JCas cas, Annotation annotation, int type )
	{
		FSIndex<?> index = cas.getAnnotationIndex( type ) ;
		return getAllContainingAnnotationsOfTypeBaseMethod(annotation, index);
	}

	protected static Collection<Annotation> getAllContainingAnnotationsOfTypeBaseMethod(
			Annotation annotation, FSIndex<?> index) {
		Annotation current;
		FSIterator<?> iterator = index.iterator() ;
		Collection<Annotation> annotations = new ArrayList<Annotation>() ;
		while ( iterator.hasNext() )
		{
			current = (Annotation) iterator.next() ;
			if ( completelyCovers( current, annotation ) )
				annotations.add( current ) ;
		}
		return annotations ;
	}

	
	/*
	 * Gets all annotations that are spanned by the Annotation annotation. Can be used to pull out all annotations in a section or a sentence
	 */
	public static Collection<Annotation> getAllCoveredAnnotations(JCas cas, Annotation annotation )
	{
		Collection<Annotation> annotations = new ArrayList<Annotation>() ;
		Annotation current = null ;
		FSIndex<?> index = cas.getAnnotationIndex() ;
		FSIterator<?> iterator = index.iterator() ;
		while ( iterator.hasNext() )
		{
			current = (Annotation) iterator.next() ;
			if ( completelyCovers( annotation, current ) )
				annotations.add( current ) ;
		}
		return annotations ;
	}

	
	/*
	 * Gets all annotations of Type type that are spanned by the Annotation annotation. Can be used to pull out all annotations in a section or a sentence
	 */
	public static Collection<Annotation> getAllCoveredAnnotationsOfType(JCas cas, Annotation annotation, Type type )
	{
		
		FSIndex<?> index = cas.getAnnotationIndex( type ) ;
		return getAllCoveredAnnotationOfTypeBaseMethod(annotation, index);
	}
	
	/*
	 * Gets all annotations of int type that are spanned by the Annotation annotation. Can be used to pull out all annotations in a section or a sentence
	 */
	public static Collection<Annotation> getAllCoveredAnnotationsOfType(JCas cas, Annotation annotation, int type )
	{
		FSIndex<?> index = cas.getAnnotationIndex( type ) ;
		return getAllCoveredAnnotationOfTypeBaseMethod(annotation, index);
	}

	protected static Collection<Annotation> getAllCoveredAnnotationOfTypeBaseMethod(
			Annotation annotation, FSIndex<?> index) {
		Annotation current;
		FSIterator<?> iterator = index.iterator() ;
		Collection<Annotation> annotations = new ArrayList<Annotation>() ;
		while ( iterator.hasNext() )
		{
			current = (Annotation) iterator.next() ;
			if ( completelyCovers( annotation, current ) )
				annotations.add( current ) ;
		}
		return annotations ;
	}
	
	
	/**
	 * Return the list of annotations of the Annotation type provided that come before the annotation object provided.
	 * maxReturn is used to limit the number of previous annotations that are returned in the list. The list is
	 * sorted so that the previous annotation that is closest to annotation comes first and the one that is furthest
	 * away comes last.
	 * 
	 * @param annotation
	 * 		The "anchor" annotation.  The list of annotations returned will all have a begin
	 * 		index that is less than the begin index of this annotation.
	 * @param type
	 * 		The type of annotations you want returned
	 * @param maxReturn
	 * 		The maximum number of annotations that will be returned, if maxReturn < 1 return all previous annotations.
	 * @return
	 */
	public static Collection<Annotation> getPreviousAnnotationsOfType(JCas cas, Annotation annotation, Type type, int maxReturn) {
		FSIndex<?> index = cas.getAnnotationIndex(type);
		return getPreviousAnnotationsOfTypeBaseMethod(annotation, maxReturn,
				index);
	}
	
	/**
	 * Return the list of annotations of the Annotation type provided that come before the annotation object provided.
	 * maxReturn is used to limit the number of previous annotations that are returned in the list. The list is
	 * sorted so that the previous annotation that is closest to annotation comes first and the one that is furthest
	 * away comes last.
	 * 
	 * @param annotation
	 * 		The "anchor" annotation.  The list of annotations returned will all have a begin
	 * 		index that is less than the begin index of this annotation.
	 * @param type
	 * 		The type of annotations you want returned
	 * @param maxReturn
	 * 		The maximum number of annotations that will be returned, if maxReturn < 1 return all previous annotations.
	 * @return
	 */
	public static Collection<Annotation> getPreviousAnnotationsOfType(JCas cas, Annotation annotation, int type, int maxReturn) {
		FSIndex<?> index = cas.getAnnotationIndex(type);
		return getPreviousAnnotationsOfTypeBaseMethod(annotation, maxReturn,
				index);
	}//getPreviousAnnotationsOfType method

	protected static Collection<Annotation> getPreviousAnnotationsOfTypeBaseMethod(
			Annotation annotation, int maxReturn, FSIndex<?> index) {
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		Annotation current = null;
		//Get the list of all the annotations that come before annotation
		
		FSIterator<?> iterator = index.iterator();
		while(iterator.hasNext()) {
			current = (Annotation) iterator.next();
			if((current == annotation) || (current.getBegin() >= annotation.getBegin()))
				break;
			annotations.add(current);
		}//while
		//Get the subset of annotations limited by maxReturn then sort the subset in descending order
		ArrayList<Annotation> previousList= new ArrayList<Annotation>();
		if(maxReturn < 1) {
			previousList.addAll(annotations);
		} else {
			int first = (annotations.size() - maxReturn > 0)? annotations.size() - maxReturn : 0;
			previousList.addAll(annotations.subList(first, annotations.size()));
		}//else
		
		Collections.sort(previousList, new Comparator<Annotation>(){
			@Override
			public int compare(Annotation arg0, Annotation arg1) {
				return ((Integer)arg1.getBegin()).compareTo((Integer)arg0.getBegin());
			}		
		});
		return previousList;
	}

	
	/**
	 * Get the list of annotations of the Annotation type provided whose begin index comes after the begin index
	 * of the annotation provided.  The size of the list is limited by the maxReturn variable, unlimited if 
	 * maxReturn < 1.  The list is sorted so that the element closest to annotation is first and the last element
	 * is farthest away from annotation.
	 * 
	 * @param annotation
	 * 		The "anchor" annotation.  The list of annotations returned will all have a begin
	 * 		index that is less than the begin index of this annotation.
	 * @param type
	 * 		The type of annotations you want returned
	 * @param maxReturn
	 * 		The maximum number of annotations that will be returned, if maxReturn < 1 return all subsequent annotations.
	 * @return
	 */
	public static Collection<Annotation> getNextAnnotationsOfType(JCas cas, Annotation annotation, Type type, int maxReturn) {
		FSIndex<Annotation> index = cas.getAnnotationIndex(type);
		
		return getNextAnnotationsOfTypeBaseMethod(annotation, maxReturn, index);		
	}
	
	/**
	 * Get the list of annotations of the Annotation type provided whose begin index comes after the begin index
	 * of the annotation provided.  The size of the list is limited by the maxReturn variable, unlimited if 
	 * maxReturn < 1.  The list is sorted so that the element closest to annotation is first and the last element
	 * is farthest away from annotation.
	 * 
	 * @param annotation
	 * 		The "anchor" annotation.  The list of annotations returned will all have a begin
	 * 		index that is less than the begin index of this annotation.
	 * @param type
	 * 		The type of annotations you want returned
	 * @param maxReturn
	 * 		The maximum number of annotations that will be returned, if maxReturn < 1 return all subsequent annotations.
	 * @return
	 */
	public static Collection<Annotation> getNextAnnotationsOfType(JCas cas, Annotation annotation, int type, int maxReturn) {
		FSIndex<Annotation> index = cas.getAnnotationIndex(type);
		
		return getNextAnnotationsOfTypeBaseMethod(annotation, maxReturn, index);
	}//getNextAnnotationsOfType method

	protected static Collection<Annotation> getNextAnnotationsOfTypeBaseMethod(
			Annotation annotation, int maxReturn, FSIndex<Annotation> index) {
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		Annotation current = null;
		//Get the list of annotations that come after this annotation, unfortunately we have to iterate until we get past
		//the annotation.getBegin();
		
		FSIterator<Annotation> iterator = index.iterator();
		int numAdded = 0;
		while(iterator.hasNext()) {
			current = (Annotation) iterator.next();
			if((current == annotation) || (current.getBegin() <= annotation.getBegin()))
				continue;
			if(maxReturn > 0 && numAdded == maxReturn)
				break;
			annotations.add(current);
			numAdded++;
		}//while
		return annotations;
	}

	/*
	 * Find the next annotations (from a collection) that immediately proceeds the Annotation annotation. The reason a collection is returned is because there
	 * may be more then one annotation starting at the same span.
	 */
	public static Collection<Annotation> getNextClosestAnnotations( Annotation annotation, Collection<Annotation> annotations )
	{
		final int MAX_SPAN = 10000 ; 
			
		Collection<Annotation> nextAnnotations = new ArrayList<Annotation>() ;
//		Annotation current = null ;
		int closestSpan = MAX_SPAN, diff = 0 ;
		for ( Annotation nextAnnotation : annotations )
		{
			if ( nextAnnotation == annotation || ( nextAnnotation.getBegin() == annotation.getBegin() && nextAnnotation.getEnd() == annotation.getEnd() ) )
				continue ;

			diff = nextAnnotation.getBegin() - annotation.getEnd() ;
			if ( diff < 0 )
				diff = MAX_SPAN + 1 ;

			if ( diff < closestSpan )
			{
				nextAnnotations.clear() ;
				nextAnnotations.add( nextAnnotation ) ;
				closestSpan = diff ;
			}
			else if ( diff == closestSpan )
			{
				nextAnnotations.add( nextAnnotation ) ;
			}
		}
		return nextAnnotations ;
	}

	/*
	 * Gets all annotations previous to annotation.
	 */
	public static Collection<Annotation> getAllPreviousAnnotations( Annotation annotation, Collection<Annotation> annotations )
	{
		Collection<Annotation> prevAnnotations = new ArrayList<Annotation>() ;

		for ( Annotation prevAnnotation : annotations )
		{
			if ( prevAnnotation == annotation || ( prevAnnotation.getBegin() == annotation.getBegin() && prevAnnotation.getEnd() == annotation.getEnd() ) )
				continue ;

			if ( annotation.getBegin() - prevAnnotation.getEnd() >= 0 )
			{
				prevAnnotations.add( prevAnnotation ) ;
			}
		}
		return prevAnnotations ;
	}

	/*
	 * Find the previous annotations (from a collection) that immediately precedes the Annotation annotation. The reason a collection is returned is because
	 * there may be more then one annotation starting at the same span.
	 */
	public static Collection<Annotation> getPreviousClosestAnnotations( Annotation annotation, Collection<Annotation> annotations )
	{
		Collection<Annotation> prevAnnotations = new ArrayList<Annotation>() ;
		int closestSpan = 10000, diff = 0 ;
		for ( Annotation prevAnnotation : annotations )
		{
			if ( prevAnnotation == annotation || coversSameSpan( prevAnnotation, annotation) )
				continue ;

			diff = annotation.getBegin() - prevAnnotation.getEnd() ;
			if ( diff < 0 )
				diff = 10001 ;

			if ( diff < closestSpan )
			{
				prevAnnotations.clear() ;
				prevAnnotations.add( prevAnnotation ) ;
				closestSpan = diff ;
			}
			else if ( diff == closestSpan )
			{
				prevAnnotations.add( prevAnnotation ) ;
			}
		}
		return prevAnnotations ;
	}

	public static boolean coversSameSpan(Annotation np1, Annotation np2)
	{
		if (np1.getBegin() == np2.getBegin() && np1.getEnd() == np2.getEnd())
			return true ;
		return false ;
	}
		
	
}
