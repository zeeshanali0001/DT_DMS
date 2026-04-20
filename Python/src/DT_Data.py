from abc import ABC, abstractmethod
from typing import Generic, TypeVar, List

T = TypeVar('T')


class ObserverData(ABC, Generic[T]):
    @abstractmethod
    def update(self, data: T):
        pass


class ObserverDataManager(ABC, Generic[T]):
    @abstractmethod
    def register_observer(self, observer: ObserverData[T]):
        pass

    @abstractmethod
    def remove_observer(self, observer: ObserverData[T]):
        pass

    @abstractmethod
    def notify_observers(self):
        pass


class DT_Data(ObserverDataManager[T], ABC, Generic[T]):

    def __init__(self, source: str, data_type: str, timestamp: str):
        self._observers: List[ObserverData[T]] = []
        self._current_data: T | None = None

        self.source = source
        self.data_type = data_type
        self.timestamp = timestamp

    # === Observer Management ===

    def register_observer(self, observer: ObserverData[T]):
        if observer not in self._observers:
            self._observers.append(observer)
            print(f"Observer registered: {observer.__class__.__name__}")

    def remove_observer(self, observer: ObserverData[T]):
        if observer in self._observers:
            self._observers.remove(observer)
            print(f"Observer removed: {observer.__class__.__name__}")

    def notify_observers(self):
        # Safe copy to avoid modification during iteration
        for observer in list(self._observers):
            observer.update(self._current_data)

    # === Data Handling ===

    def update_data(self, new_data: T):
        self._current_data = new_data
        print(f"\n[DATA SOURCE] New data received: {new_data}")
        self.notify_observers()

    def get_current_data(self) -> T | None:
        return self._current_data

    # === Abstract Method ===

    @abstractmethod
    def fetch_data(self):
        pass

    # === Validation ===

    def validate_data(self) -> bool:
        return all([
            self.source,
            self.data_type,
            self.timestamp
        ])

    # === Getters / Setters ===

    def get_source(self) -> str:
        return self.source

    def set_source(self, source: str):
        self.source = source

    def get_data_type(self) -> str:
        return self.data_type

    def set_data_type(self, data_type: str):
        self.data_type = data_type

    def get_timestamp(self) -> str:
        return self.timestamp

    def set_timestamp(self, timestamp: str):
        self.timestamp = timestamp

    # === Object Overrides ===

    def __str__(self):
        return (
            f"DT_Data(source='{self.source}', "
            f"data_type='{self.data_type}', "
            f"timestamp='{self.timestamp}')"
        )

    def __eq__(self, other):
        if not isinstance(other, DT_Data):
            return False
        return (
            self.source == other.source and
            self.data_type == other.data_type and
            self.timestamp == other.timestamp
        )

    def __hash__(self):
        return hash((self.source, self.data_type, self.timestamp))
