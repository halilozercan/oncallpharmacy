//
//  DashboardView.swift
//  iosApp
//
//  Created by Çağatay Emekci on 20.12.2020.
//  Copyright © 2020 orgName. All rights reserved.
//

import SwiftUI
import MapKit
import shared
import Combine
import ComposableArchitecture

struct DashboardView: View {
    
    let images: [Image] = [.iconsBandage, .iconsDose, .iconsHandWithAPill, .iconsPillBottle]
    
    @State var showingPreview = false
    @State private var userTrackingMode: MKUserTrackingMode = .none
    @State private var selectedCoordinate: CLLocationCoordinate2D = CLLocationCoordinate2D(latitude: 39, longitude: 42)
    let mapViewContianer = MapViewContainer()
    let store: Store<MapState, MapAction>
    
    var body: some View {
        WithViewStore(self.store) { viewStore in
            ZStack {
                Color.backColor
                ZStack {
                    MapView(
                        userTrackingMode: $userTrackingMode,
                        selectLocation: $selectedCoordinate,
                        annotations: viewStore.annotations,
                        didSelect: { annotation in
                            viewStore.send(.selectAnnotation(annotation))
                        }, removeAllAnnotation: {
                            viewStore.send(.removeAllAnnotation)
                        }, regionWillChange: {
                            viewStore.send(.regionWillChange)
                        }, regionDidChange: { center, bounds in
                            viewStore.send(.regionDidChange(center, bounds))
                        }
                    )
                    .environmentObject(MapViewContainer())
                    .edgesIgnoringSafeArea(.all)
                    .onTapGesture {
                        viewStore.send(.showDetail(false))
                    }
                    VStack {
                        Spacer()
                        if viewStore.showingDetail {
                            if case .detail = viewStore.screenState {
                                ScrollView (.horizontal, showsIndicators: false) {
                                    HStack {
                                        ForEach(viewStore.pharmacies, id: \.self) { phar in
                                            PharmacyDetail(pharmacy: phar)
                                                .onTapGesture {
                                                    selectedCoordinate = CLLocationCoordinate2D(latitude: phar.latitude, longitude: phar.longitude)
                                                }
                                        }
                                    }
                                }
                                .frame(height: 240)
                            }
                        } else {
                            if !(userTrackingMode == .follow || userTrackingMode == .followWithHeading) {
                                ZStack {
                                    Color.black
                                        .frame(width: 100, height: 100)
                                        .clipShape(Circle())
                                        .opacity(0.1)
                                    
                                    Button(action: { self.followUser() }) {
                                        Image(systemName: "location.fill")
                                            .modifier(MapButton(backgroundColor: .primary))
                                    }
                                }
                                .alignmentGuide(.bottom) { d in
                                    d[.bottom] + 20
                                }
                                .frame(height: 150)
                            }
                        }
                    }
                }
                
            }
        }
    }
    
    private func followUser() {
        userTrackingMode = .follow
    }
}
extension DashboardView {
    ///Path to device settings if location is disabled
    func goToDeviceSettings() {
        guard let url = URL.init(string: UIApplication.openSettingsURLString) else { return }
        UIApplication.shared.open(url, options: [:], completionHandler: nil)
    }
}

fileprivate struct MapButton: ViewModifier {
    
    let backgroundColor: Color
    var fontColor: Color = Color(UIColor.systemBackground)
    
    func body(content: Content) -> some View {
        content
            .padding()
            .background(self.backgroundColor.opacity(0.9))
            .foregroundColor(self.fontColor)
            .font(.title)
            .clipShape(Circle())
    }
}
