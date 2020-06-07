package chess.piecebuilder;

import java.lang.reflect.Method;

import chess.util.AFC;
import chess.util.BooleanPath;
import chess.util.InputVerification;
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
	public static BuildFinisher getFinisherFor(PathBuilder builder) {
		if(builder instanceof BooleanPathBuilder) {
			return on((BooleanPathBuilder) builder);
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
	
	private static BoolBuildFinisher on(BooleanPathBuilder builder) {
		return new BoolBuildFinisher(builder);
	}
	
	private static IntegerBuildFinisher on(IntegerPathBuilder builder) {
		return new IntegerBuildFinisher(builder);
	}
	
	private static ObjectBuildFinisher on(ObjectPathBuilder builder) {
		return new ObjectBuildFinisher(builder);
	}
	
	public BuildFinisher() {
		super();
		this.setConverter((StringConverter<Method>) ConditionBox.memberStringConverter);
	}
	
	//Overridden to add functionality
	public void postAdd() {}
	
	public abstract PathBuilder getPrecedingBuilder();
	
	public static class BoolBuildFinisher extends BuildFinisher implements InputVerification{
		private BooleanPathBuilder precedingBuilder;
		public BoolBuildFinisher(BooleanPathBuilder precedingBuilder) {
			super();
			this.precedingBuilder = precedingBuilder;
			ObservableList<Method> items = this.getItems();
			for(Method m : BooleanPath.class.getMethods()) {
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
			        CustomConditionBox ccb = (CustomConditionBox) precedingBuilder.getParent();
			        int myIndex = ccb.getChildren().indexOf(BoolBuildFinisher.this);
			        ConditionBox.clearPast(ccb.getChildren(), myIndex);
			        for(int i = 0; i < paramTypes.length; i++) {
			        	if(paramTypes[i] == BooleanPath.class) {
			        		ccb.addBuilder(PathBuilder.BOOLEAN_BUILDER, false);
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
		@Override
		public boolean verifyInput() {
			boolean result = true;
			if(BoolBuildFinisher.this.getSelectionModel().isEmpty()) {
				PieceBuilder.submitError("Boolean Path finisher has no selection");
				result = false;
			}
			return result;
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
			        CustomConditionBox ccb = (CustomConditionBox) precedingBuilder.getParent();
			        int myIndex = ccb.getChildren().indexOf(IntegerBuildFinisher.this);
			        ConditionBox.clearPast(ccb.getChildren(), myIndex);
			        for(int i = 0; i < paramTypes.length; i++) {
			        	if(paramTypes[i] == IntegerPath.class) {
			        		ccb.addBuilder(PathBuilder.INTEGER_BUILDER, false);
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
			((CustomConditionBox) precedingBuilder.getParent()).addBuilder(PathBuilder.INTEGER_BUILDER, false);
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
					if(m.getName().equals("isNotNull")) {
						this.setValue(m);
					}
				}
			}
			
			ObjectBuildFinisher.this.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			      @Override
			      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
			        Method choice = ObjectBuildFinisher.this.getItems().get((Integer) number2);
			        Class<?>[] paramTypes = choice.getParameterTypes();
			        CustomConditionBox ccb = (CustomConditionBox) precedingBuilder.getParent();
			        int myIndex = ccb.getChildren().indexOf(ObjectBuildFinisher.this);
			        ConditionBox.clearPast(ccb.getChildren(), myIndex);
			        for(int i = 0; i < paramTypes.length; i++) {
			        	if(paramTypes[i] == ObjectPath.class) {
			        		ccb.addBuilder(PathBuilder.OBJECT_BUILDER, false);
			        	}
			        	else if(paramTypes[i] == Class.class) {
			        		ccb.getChildren().add(new ConditionActionChooser());
			        	}
			        	else if(paramTypes[i] == String.class && choice.getName().equals("isPiece")){
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
		public PathBuilder getPrecedingBuilder() {
			return precedingBuilder;
		}
	}
}
