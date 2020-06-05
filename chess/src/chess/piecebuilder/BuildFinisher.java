package chess.piecebuilder;

import java.lang.reflect.Method;

import chess.util.AFC;
import chess.util.BoolPath;
import chess.util.IntegerPath;
import chess.util.ObjectPath;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

public abstract class BuildFinisher extends ChoiceBox<Method> {
	public static BuildFinisher on(PathBuilder builder) {
		if(builder instanceof BoolPathBuilder) {
			return on((BoolPathBuilder) builder);
		}
		else if(builder instanceof IntegerPathBuilder) {
			return on((IntegerPathBuilder) builder);
		}
		else if(builder instanceof ObjectPathBuilder) {
			return on((ObjectPathBuilder) builder);
		}
		else {
			throw new IllegalArgumentException("BuildFinisher does not support this type");
		}
	}
	
	public static BoolBuildFinisher on(BoolPathBuilder builder) {
		return new BoolBuildFinisher(builder);
	}
	
	public static IntegerBuildFinisher on(IntegerPathBuilder builder) {
		return new IntegerBuildFinisher(builder);
	}
	
	public static ObjectBuildFinisher on(ObjectPathBuilder builder) {
		return new ObjectBuildFinisher(builder);
	}
	
	public BuildFinisher() {
		super();
		this.setConverter((StringConverter<Method>) ConditionBox.memberStringConverter);
	}
	
	//Overridden to add functionality
	public void postAdd() {}
	
	public abstract PathBuilder getPrecedingBuilder();
	
	public static class BoolBuildFinisher extends BuildFinisher{
		private BoolPathBuilder precedingBuilder;
		public BoolBuildFinisher(BoolPathBuilder precedingBuilder) {
			super();
			this.precedingBuilder = precedingBuilder;
			ObservableList<Method> items = this.getItems();
			for(Method m : BoolPath.class.getMethods()) {
				if(m.isAnnotationPresent(AFC.class)) {
					//AFC afc = m.getAnnotation(AFC.class);
					items.add(m);
					if(m.getName().equals("toCond")) {
						this.setValue(m);
					}
				}
			}
			BoolBuildFinisher.this.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			      @Override
			      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
			        Method choice = BoolBuildFinisher.this.getItems().get((Integer) number2);
			        Class<?>[] paramTypes = choice.getParameterTypes();
			        boolean nodeAdded = false;
			        for(int i = 0; i < paramTypes.length; i++) {
			        	if(paramTypes[i] == BoolPath.class) {
			        		CustomConditionBox ccb = (CustomConditionBox) precedingBuilder.nodeToAddTo;
			        		if(!nodeAdded) {
			        			int myIndex = ccb.getChildren().indexOf(BoolBuildFinisher.this);
				        		ConditionBox.clearPast(ccb.getChildren(), myIndex);
				        		nodeAdded = true;
			        		}
			        		ccb.addDropPathPane("bool", false);
			        	}
			        	else {
			        		throw new UnsupportedOperationException("Parameter type " + paramTypes[i] + " is not supported");
			        	}
			        }
			      }
			    });
		}
		@Override
		public PathBuilder getPrecedingBuilder() {
			return precedingBuilder;
		}
	}
	
	public static class IntegerBuildFinisher extends BuildFinisher{
		private IntegerPathBuilder precedingBuilder;
		public IntegerBuildFinisher(IntegerPathBuilder precedingBuilder) {
			super();
			this.precedingBuilder = precedingBuilder;
			ObservableList<Method> items = this.getItems();
			for(Method m : IntegerPath.class.getMethods()) {
				if(m.isAnnotationPresent(AFC.class)) {
					//AFC afc = m.getAnnotation(AFC.class);
					items.add(m);
					if(m.getName().equals("isEquals")) {
						this.setValue(m);
					}
				}
			}
			
			IntegerBuildFinisher.this.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			      @Override
			      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
			        Method choice = IntegerBuildFinisher.this.getItems().get((Integer) number2);
			        Class<?>[] paramTypes = choice.getParameterTypes();
			        boolean nodeAdded = false;
			        for(int i = 0; i < paramTypes.length; i++) {
			        	if(paramTypes[i] == IntegerPath.class) {
			        		CustomConditionBox ccb = (CustomConditionBox) precedingBuilder.nodeToAddTo;
			        		if(!nodeAdded) {
			        			int myIndex = ccb.getChildren().indexOf(IntegerBuildFinisher.this);
				        		ConditionBox.clearPast(ccb.getChildren(), myIndex);
				        		nodeAdded = true;
			        		}
			        		ccb.addDropPathPane("integer", false);
			        	}
			        	else {
			        		throw new UnsupportedOperationException("Parameter type " + paramTypes[i] + " is not supported");
			        	}
			        }
			      }
			    });
		}
		
		@Override
		public void postAdd() {
			((CustomConditionBox) precedingBuilder.nodeToAddTo).addDropPathPane("integer", false);
		}
		@Override
		public PathBuilder getPrecedingBuilder() {
			return precedingBuilder;
		}
	}
	
	public static class ObjectBuildFinisher extends BuildFinisher{
		private ObjectPathBuilder precedingBuilder;
		public ObjectBuildFinisher(ObjectPathBuilder precedingBuilder) {
			super();
			this.precedingBuilder = precedingBuilder;
			ObservableList<Method> items = this.getItems();
			for(Method m : ObjectPath.class.getMethods()) {
				if(m.isAnnotationPresent(AFC.class)) {
					//AFC afc = m.getAnnotation(AFC.class);
					items.add(m);
					if(m.getName().equals("referenceEquals")) {
						this.setValue(m);
					}
				}
			}
			
			ObjectBuildFinisher.this.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			      @Override
			      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
			        Method choice = ObjectBuildFinisher.this.getItems().get((Integer) number2);
			        Class<?>[] paramTypes = choice.getParameterTypes();
			        CustomConditionBox ccb = (CustomConditionBox) precedingBuilder.nodeToAddTo;
			        int myIndex = ccb.getChildren().indexOf(ObjectBuildFinisher.this);
			        ConditionBox.clearPast(ccb.getChildren(), myIndex + paramTypes.length);
			        boolean nodeAdded = false;
			        for(int i = 0; i < paramTypes.length; i++) {
			        	if(paramTypes[i] == ObjectPath.class) {
			        		if(!nodeAdded) {
				        		ConditionBox.clearPast(ccb.getChildren(), myIndex);
				        		nodeAdded = true;
			        		}
			        		ccb.addDropPathPane("object", false);
			        	}
			        	else if(paramTypes[i] == Class.class) {
			        		if(!nodeAdded) {
				        		ConditionBox.clearPast(ccb.getChildren(), myIndex);
				        		nodeAdded = true;
			        		}
			        		ccb.getChildren().add(new ConditionActionChooser());
			        	}
			        	else if(paramTypes[i] == String.class && choice.getName().equals("isPiece")){
			        		if(!nodeAdded) {
				        		ConditionBox.clearPast(ccb.getChildren(), myIndex);
				        		nodeAdded = true;
			        		}
			        		ccb.getChildren().add(new ConditionPieceChooser());
			        	}
			        	else {
			        		throw new UnsupportedOperationException("Parameter type " + paramTypes[i] + " is not supported");
			        	}
			        }
			      }
			    });
		}
		
		@Override
		public void postAdd() {
			((CustomConditionBox) precedingBuilder.nodeToAddTo).addDropPathPane("object", false);
		}
		@Override
		public PathBuilder getPrecedingBuilder() {
			return precedingBuilder;
		}
	}
}
